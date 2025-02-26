/**
 * Copyright (c) 2016-2019 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.example.locks;

import org.redisson.Redisson;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class RedLockExamples {

    public static void main(String[] args) throws InterruptedException {
        // connects to 127.0.0.1:6379 by default
        RedissonClient client1 = Redisson.create();
        RedissonClient client2 = Redisson.create();
        
        RLock lock1 = client1.getLock("lock1");
        RLock lock2 = client1.getLock("lock2");
        RLock lock3 = client2.getLock("lock3");
        
        Thread t1 = new Thread(lock3::lock);
        t1.start();
        t1.join();
        
        Thread t = new Thread(() -> {
			RedissonMultiLock lock = new RedissonRedLock(lock1, lock2, lock3);
			lock.lock();

			try {
				Thread.sleep(3000);
			} catch (InterruptedException ignored) {
			}
			lock.unlock();
		});
        t.start();
        t.join(1000);

        lock3.forceUnlock();
        
        RedissonMultiLock lock = new RedissonRedLock(lock1, lock2, lock3);
        lock.lock();
        lock.unlock();

        client1.shutdown();
        client2.shutdown();
    }
    
}
