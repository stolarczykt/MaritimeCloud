/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maritimecloud.internal.net.client.broadcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.maritimecloud.internal.net.client.AbstractClientConnectionTest;
import net.maritimecloud.internal.net.client.broadcast.stubs.HelloWorld;
import net.maritimecloud.internal.net.messages.c2c.broadcast.BroadcastAck;
import net.maritimecloud.internal.net.messages.c2c.broadcast.BroadcastSend;
import net.maritimecloud.internal.net.messages.c2c.broadcast.BroadcastSendAck;
import net.maritimecloud.net.MaritimeCloudClient;
import net.maritimecloud.net.broadcast.BroadcastFuture;
import net.maritimecloud.net.broadcast.BroadcastMessage;
import net.maritimecloud.net.broadcast.BroadcastMessage.Ack;
import net.maritimecloud.net.broadcast.BroadcastSendOptions;
import net.maritimecloud.util.function.Consumer;
import net.maritimecloud.util.geometry.PositionTime;

import org.junit.Test;

/**
 * Tests the future returned by {@link MaritimeCloudClient#broadcast(BroadcastMessage)} and
 * {@link MaritimeCloudClient#broadcast(BroadcastMessage, BroadcastSendOptions)}.
 * 
 * @author Kasper Nielsen
 */
public class BroadcastFutureTest extends AbstractClientConnectionTest {

    @Test
    public void broadcastServerAck() throws Exception {
        MaritimeCloudClient c = createAndConnect();

        BroadcastFuture bf = c.broadcast(new HelloWorld("hello"));
        BroadcastSend mb = t.take(BroadcastSend.class);
        assertEquals("hello", ((HelloWorld) mb.tryRead()).getMessage());

        BroadcastSendAck bsa = new BroadcastSendAck(mb.getReplyTo());
        t.send(bsa);

        // make sure it is received on the server
        bf.receivedOnServer().get(1, TimeUnit.SECONDS);

        try {
            bf.onAck(Consumer.NOOP);
            fail("Should throw UOE, when client ack is not on");
        } catch (UnsupportedOperationException ok) {}

    }

    @Test
    public void broadcastClientAcks() throws Exception {
        MaritimeCloudClient c = createAndConnect();

        BroadcastSendOptions options = new BroadcastSendOptions();
        options.setReceiverAckEnabled(true);

        BroadcastFuture bf = c.broadcast(new HelloWorld("hello"), options);
        BroadcastSend mb = t.take(BroadcastSend.class);
        assertEquals("hello", ((HelloWorld) mb.tryRead()).getMessage());

        BroadcastSendAck bsa = new BroadcastSendAck(mb.getReplyTo());
        t.send(bsa);

        // make sure it is received on the server
        bf.receivedOnServer().get(1, TimeUnit.SECONDS);

        final BlockingQueue<BroadcastMessage.Ack> q = new LinkedBlockingQueue<>();
        BroadcastAck ba = new BroadcastAck(mb.getReplyTo(), ID3, PositionTime.create(3, 3, 3));
        t.send(ba);

        bf.onAck(new Consumer<BroadcastMessage.Ack>() {
            public void accept(Ack t) {
                q.add(t);
            }
        });

        ba = new BroadcastAck(mb.getReplyTo(), ID4, PositionTime.create(4, 4, 4));
        t.send(ba);

        Ack a3 = q.poll(1, TimeUnit.SECONDS);
        assertEquals(ID3, a3.getId());
        assertEquals(PositionTime.create(3, 3, 3), a3.getPosition());

        Ack a4 = q.poll(1, TimeUnit.SECONDS);
        assertEquals(ID4, a4.getId());
        assertEquals(PositionTime.create(4, 4, 4), a4.getPosition());

        assertTrue(q.isEmpty());
    }
}
