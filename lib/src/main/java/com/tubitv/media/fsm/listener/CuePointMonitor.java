package com.tubitv.media.fsm.listener;

import android.support.annotation.Nullable;
import android.util.Log;

import com.tubitv.media.fsm.Input;
import com.tubitv.media.fsm.concrete.AdPlayingState;
import com.tubitv.media.fsm.state_machine.FsmPlayer;
import com.tubitv.media.helpers.Constants;

/**
 * Created by allensun on 8/7/17.
 * on Tubitv.com, allengotstuff@gmail.com
 * <p>
 * This class monitors the ExoPlayer frame by frame playing progress, and handles {@link com.tubitv.media.fsm.state_machine.FsmPlayer} and its two {@link com.tubitv.media.fsm.Input}
 * <p>
 * 1. somme abstract two start making the network call to the server relative to cue point
 * <p>
 * 2. when at the cue point, show ads.
 */
public abstract class CuePointMonitor {

    public FsmPlayer fsmPlayer;

    private static final String TAG = CuePointMonitor.class.getSimpleName();

    /**
     * this is the safe check to only call ad work one time given that the range check can make multiple add call for one cuepoint.
     */
    private boolean safeCheckForAdcall = true;

    private boolean safeCheckForCue = true;

    private long[] cuePoints;

    private long[] adCallPoints;

    /**
     * keep track of current relevant cue point this time,
     */
    private int currentQueuePointPos = -1;

    public abstract int networkingAhead();

    public CuePointMonitor(FsmPlayer fsmPlayer) {
        this.fsmPlayer = fsmPlayer;
    }

    public void setQuePoints(@Nullable long[] cuePoints) {

        currentQueuePointPos = -1;

        if (cuePoints == null) {
            this.cuePoints = null;
            adCallPoints = null;
            return;
        }

        this.cuePoints = cuePoints;
        adCallPoints = getAddCallPoints(cuePoints);
    }


    /**
     * remove the cuepoint that already shown, only when a add call is finished.
     *
     * Not used
     */
    public void remoteShowedCuePoints() {

        if (cuePoints == null || cuePoints.length <= 0) {
            return;
        }

        //when only remain the last one.
        if (cuePoints.length == 1) {
            setQuePoints(null);
            return;
        }

        long[] newcuePoints = removeElementFromArray(cuePoints,currentQueuePointPos);

        if(newcuePoints!=null){
            setQuePoints(newcuePoints);
        }
    }

    @Nullable
    private long[] removeElementFromArray(long[] array, int keyPos) {

        if (array == null || keyPos < 0 || array.length <= 1) {
            return null;
        }

        int length = array.length - 1;

        long[] result = new long[length];

        int tempPos = 0;
        for (int i = 0; i < length; i++) {

            if (i == keyPos) {
                tempPos++;
            }

            result[i] = array[tempPos];

            tempPos++;
        }

        return result;
    }

    /**
     * this method will update frame by frame on movie millisecond to check if any action can be triggered
     *
     * @param milliseconds
     * @param durationMillis
     */
    public void onMovieProgress(long milliseconds, long durationMillis) {

        if (fsmPlayer.getCurrentState() instanceof AdPlayingState) {
            // if ad playing, do nothing
            return;
        }
        //check if need to request ad call, if does, update the fsmPlayer, Request_AD.
        preformAdCallIfNecessary(milliseconds);

        //check if need to show ad, id does, update the fsmPlayer to Show_AD
        preformShowAdIfNecessary(milliseconds);
    }

    private void preformShowAdIfNecessary(long milliseconds) {
        if (isProgressActionable(cuePoints, milliseconds) && safeCheckForCue) {
            safeCheckForCue = false;
            Log.d(Constants.FSMPLAYER_TESTING, "Show ads at : " + milliseconds);
            fsmPlayer.transit(Input.SHOW_ADS);
            return;
        } else if (!isProgressActionable(cuePoints, milliseconds)) {
            safeCheckForCue = true;
        }
    }

    private void preformAdCallIfNecessary(long milliseconds) {
        if (isProgressActionable(adCallPoints, milliseconds) && safeCheckForAdcall) {
            safeCheckForAdcall = false;

            //update the cuepoint;
            if (currentQueuePointPos >= 0) {
                long currentQueuePoint = cuePoints[currentQueuePointPos];

                // update the cue point infor to AdRetriever and FsmPlayer status.
                fsmPlayer.updateCuePointForRetriever(currentQueuePoint);
                Log.d(Constants.FSMPLAYER_TESTING, "make network call at: " + milliseconds);
                fsmPlayer.transit(Input.MAKE_AD_CALL);
                return;
            }

        } else if (!isProgressActionable(adCallPoints, milliseconds)) {
            safeCheckForAdcall = true;
        }
    }

    /**
     * check if current progress millisecond is capable of trigger action
     *
     * @param array           array of relevant check points.
     * @param currentProgress current milliSecond of movie
     * @return is current millisecond need to trigger action.
     */
    private boolean isProgressActionable(long[] array, long currentProgress) {
        if (array == null || array.length <= 0) {
            currentQueuePointPos = -1;
            return false;
        }

        int resultPos = binarySerchWithRange(array, currentProgress);

        if (resultPos < 0) {
            currentQueuePointPos = -1;
            return false;
        }

        currentQueuePointPos = resultPos;
        return true;
    }


    private static final long RANGE_FACTOR = 1500;

    public static int binarySerchWithRange(long[] a, long key) {
        return binarySearchWithRange(a, 0, a.length, key, RANGE_FACTOR);
    }

    public static int binarySerchExactly(long[] a, long key) {
        return binarySearchWithRange(a, 0, a.length, key, 0);
    }

    // Like public version, but without range checks.
    private static int binarySearchWithRange(long[] a, int fromIndex, int toIndex,
                                             long key, long range_factor) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = a[mid];

            if (midVal + range_factor < key)
                low = mid + 1;
            else if (midVal - range_factor > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    /**
     * create add call points array base on queuePoints
     *
     * @param cuePoints the cuePoints of the movie
     * @return ad call points
     */
    private long[] getAddCallPoints(long[] cuePoints) {
        long[] array = new long[cuePoints.length];
        for (int i = 0; i < cuePoints.length; i++) {
            long temp = cuePoints[i] - networkingAhead();

            //minimum networking call is 0 millisecond.
            array[i] = temp > 0 ? temp : 0;
        }//end for
        return array;
    }


}