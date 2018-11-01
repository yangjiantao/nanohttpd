package org.nanohttpd.util;

/*
 * #%L
 * NanoHttpd-Webserver
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.nanohttpd.protocols.http.NanoHTTPD;

public class ServerRunner {

    /**
     * logger to log to.
     */
    private volatile static CountDownLatch downLatch;

    private static final Object lockObj = new Object();
    private static final String TAG = "ServerRunner";

    public static void executeInstance(NanoHTTPD server) {
        executeInstance(server, null);
    }

    public static void executeInstance(NanoHTTPD server, ServerRunStateListener listener) {
        if (downLatch != null) {
            System.out.println("Server is already started.\n");
            return;
        }
        try {
            downLatch = new CountDownLatch(1);
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            try {
                System.out.println("server is started.");
                if (listener != null) {
                    listener.onStartSuccess(server.getHostname(), server.getListeningPort());
                }
                if (downLatch != null) {
                    synchronized (lockObj) {
                        if (downLatch != null) {
                            downLatch.await();
                        }
                    }
                }
            } catch (InterruptedException ignore) {
            } finally {
                System.out.println("Server is stopped.");
                server.stop();
            }
        } catch (IOException ioe) {
            if (listener != null) {
                listener.onStartFailed(ioe.getMessage());
            }
            System.out.println("Couldn't start server:\n" + ioe);
            downLatch = null;
        }
    }

    public static void stopServer() {
        if (downLatch == null) {
            System.out.println("Server not started");
            return;
        }
        downLatch.countDown();
        synchronized (lockObj) {
            downLatch = null;
        }
    }

    public static <T extends NanoHTTPD> void run(Class<T> serverClass) {
        try {
            executeInstance(serverClass.newInstance());
        } catch (Exception e) {
            System.out.println("Could not create server :exception " + e);
        }
    }

    public interface ServerRunStateListener {
        void onStartSuccess(String ipHost, int port);

        void onStartFailed(String msg);
    }
}
