
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

package com.csz.handler.base;

public class Message {
    static final int FLAG_IN_USE = 1 << 0;

    public int what;
    public int flags = 0;
    public Object obj;
    public long when = 0;
    public Message next;

    public Handler target;
    public Runnable callback;

    public static final Object sPoolSync = new Object();
    private static Message sPool;
    private static int sPoolSize = 0;

    private static final int MAX_POOL_SIZE = 50;

    private static boolean gCheckRecycle = true;

    public Message() {
    }

    public static Message obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                Message m = sPool;
                sPool = m.next;
                m.next = null;
                m.flags = 0; // clear in-use flag
                sPoolSize--;
                return m;
            }
        }
        return new Message();
    }

    public static Message obtain(int what,Object obj,Handler handler) {
        Message m = obtain();
        m.what = what;
        m.obj = obj;
        m.target = handler;
        return m;
    }

    public static Message obtain(Message orig) {
        Message m = obtain();
        m.what = orig.what;
        m.obj = orig.obj;
        m.callback = orig.callback;
        m.target = orig.target;
        return m;
    }

    public void recycle() {
        if (isInUse()) {
            if (gCheckRecycle) {
                throw new IllegalStateException("This message cannot be recycled because it "
                        + "is still in use.");
            }
            return;
        }
        recycleUnchecked();
    }

    void recycleUnchecked() {
        // Mark the message as in use while it remains in the recycled object pool.
        flags = FLAG_IN_USE;
        what = 0;
        when = 0;
        obj = null;
        target = null;
        callback = null;

        synchronized (sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    boolean isInUse() {
        return ((flags & FLAG_IN_USE) == FLAG_IN_USE);
    }

    void markInUse() {
        flags |= FLAG_IN_USE;
    }

    public void sendToTarget() {
        target.sendMessage(this);
    }

    @Override
    public String toString() {
        return "Message{" + "what=" + what + ", flags=" + flags + ", obj=" + obj + '}';
    }
}
