package io.confluent.kpay.control;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectWaitControllable implements Controllable {


    long waitingSince;
    boolean paused = false;
    private final Lock lock = new ReentrantLock();
    private final Condition isPaused = lock.newCondition();

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean pauseMaybe() {
        lock.lock();
        try {
            if (paused) {
                try {
                    isPaused.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public void startProcessing() {
        lock.lock();
        try {

            paused = false;
            isPaused.signalAll();
        } finally {
            waitingSince = 0;
            lock.unlock();
        }
    }

    @Override
    public void pauseProcessing() {
        lock.lock();
        try {
            paused = true;
        } finally {
            waitingSince = System.currentTimeMillis();
            lock.unlock();
        }

    }

    public long getWaitingElaspedMs() {
        if (waitingSince == 0) return 0;
        return System.currentTimeMillis() - waitingSince;
    }
}
