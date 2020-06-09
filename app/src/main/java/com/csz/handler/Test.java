/*
 * Copyright (C) 2020 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  author:caishuzhan  github:https://github.com/zsgfrtttt
 */

package com.csz.handler;


import com.csz.handler.base.Handler;
import com.csz.handler.base.Looper;
import com.csz.handler.base.Message;

import java.util.concurrent.CountDownLatch;

/**
 * 测试Handler
 */
public class Test {


    public static void main(String[] args) {
        Looper.prepareMainLooper();

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                System.out.println(msg.toString() + "  - >  " + Thread.currentThread().getName());
            }
        };

        CountDownLatch latch = new CountDownLatch(1);

        //发送消息给主线程
        ActivityThread thread1 = new ActivityThread(handler,100L);
        thread1.latch = latch;
        thread1.start();

        Handler thread1Handler = thread1.getHandler();
        //发送消息通知子线程thread1
        ActivityThread thread2 = new ActivityThread(thread1Handler,100L);
        thread2.start();

        Looper.loop();
    }

    public static final class ActivityThread extends Thread {

        final Handler mHandler;
        final long sleep;
        H mH;

        public CountDownLatch latch;

        ActivityThread(Handler handler,long sleep) {
            this.mHandler = handler;
            this.sleep = sleep;
        }

        @Override
        public void run() {
            if (latch != null) latch.countDown();
            Looper.prepare();
            mH = new H();
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message.obtain(10, "run in " + Thread.currentThread().getName(), mHandler).sendToTarget();
            Looper.myLooper().loop();
        }

        final Handler getHandler() {
            return mH;
        }

        class H extends Handler {
            @Override
            public void handleMessage(Message msg) {
                System.out.println(msg.toString() + "  -   " + Thread.currentThread().getName());
            }
        }
    }
}
