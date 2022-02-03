/*******************************************************************************
 * © Copyright 2017-2022 Micro Focus or one of its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.octane.ideplugins.eclipse.util;

import java.util.Timer;
import java.util.TimerTask;

public class DelayedRunnable {

    private Timer fireEventTimer = new Timer();
    private int delay = 500;
    private Runnable runnable;

    public DelayedRunnable(Runnable runnable, int delay) {
        this.delay = delay;
        this.runnable = runnable;
    }

    public void execute() {
        fireEventTimer.cancel();
        fireEventTimer = new Timer();
        fireEventTimer.schedule(createTask(), delay);
    }

    private TimerTask createTask() {
        return new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

}
