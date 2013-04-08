/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tjumyk.thread;

import javafx.application.Platform;

/**
 *
 * @author tjumyk
 */
public abstract class BackgroundRunner {

    private boolean waitForeground = false;

    public abstract void background() throws Exception;

    public void foreground() throws Exception {
    }

    public abstract void handleException(Exception e) throws Exception;

    public void finish() throws Exception{
    	
    }

    protected final void runForeground() {
        waitForeground = true;
        Platform.runLater(new Thread() {
            @Override
            public void run() {
                try {
                    foreground();
                } catch (Exception ex) {
                    try {
                        handleException(ex);
                    } catch (Exception ex1) {
                        ex1.printStackTrace();
                    }
                }
                waitForeground = false;
            }
        });
        while (waitForeground) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleExceptionInFxThread(final Exception e) {
        Platform.runLater(new Thread() {
            @Override
            public void run() {
                try {
                    handleException(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public final void run() {
        new Thread() {
            @Override
            public void run() {
                try {
                    background();
                    Platform.runLater(new Thread() {
                        @Override
                        public void run() {
                            try {
                                finish();
                            } catch (Exception ex) {
                                try {
                                    handleException(ex);
                                } catch (Exception ex1) {
                                    ex1.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (final Exception e) {
                    handleExceptionInFxThread(e);
                }
            }
        }.start();
    }
}
