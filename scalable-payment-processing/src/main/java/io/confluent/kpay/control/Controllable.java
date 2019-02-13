package io.confluent.kpay.control;

public interface Controllable {

    /**
     * Whatever needs to stop and start will block on this call
     * @return
     */
    boolean pauseMaybe();

    /**
     * Event hooks to control start and stop
     */
    void startProcessing();
    void pauseProcessing();

    boolean isPaused();
}
