/*
 * Copyright (c) 2010-2012 Grid Dynamics Consulting Services, Inc, All Rights Reserved
 * http://www.griddynamics.com
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.griddynamics.jagger.engine.e1.scenario;

import com.griddynamics.jagger.coordinator.NodeId;
import com.griddynamics.jagger.user.ProcessingConfig;
import com.griddynamics.jagger.util.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: dkotlyarov
 */
public class UserGroup {
    private static final Logger log = LoggerFactory.getLogger(UserGroup.class);

    private final int id;
    private final long life;
    private final UserClock clock;
    final ArrayList<User> users;
    int activeUserCount = 0;
    int startedUserCount = 0;

    private final FunctionOfTime usersCount;

    public UserGroup(UserClock clock, int id, ProcessingConfig.Test.Task.User config, long time) {
        this(   clock,
                id,
                Parser.parseInt(config.getCount(), clock.getRandom()),
                Parser.parseInt(config.getStartCount(), clock.getRandom()),
                time + Parser.parseTime(config.getStartIn(), clock.getRandom()),
                Parser.parseTime(config.getStartBy(), clock.getRandom()),
                Parser.parseTime(config.getWarmUpTime(), clock.getRandom()),
                Parser.parseTime(config.getLife(), clock.getRandom())
        );
    }

    public UserGroup(UserClock clock, int id, int count, int startCount, long startInTime, long startBy, long warmUpTime, long life) {
        this(clock, id, count, startInTime, (warmUpTime != -1 ? warmUpTime : (count*startBy)/startCount - startBy), life);
    }

    public UserGroup(UserClock clock, int id, int count, long startInTime, long warmUpTime, long life) {
        this.clock = clock;
        this.id = id;
        this.users = new ArrayList<User>(count);
        this.life = life;
        this.usersCount = new RumpUp(new BigDecimal(count), warmUpTime, startInTime, "Users");

        log.info(String.format("User group %d is created", id));
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return usersCount.getDisplayValue().intValue();
    }

    public long getLife() {
        return life;
    }

    public int getActiveUserCount() {
        return activeUserCount;
    }

    public void tick(long time, LinkedHashMap<NodeId, WorkloadConfiguration> workloadConfigurations) {
        for (User user : users) {
            user.tick(time, workloadConfigurations);
        }

        int diff = usersCount.get(time).intValue() - users.size();
        if (diff > 0) {
            spawnUsers(diff, time, workloadConfigurations);
        }
    }

    public void spawnUsers(int userCount, long time, LinkedHashMap<NodeId, WorkloadConfiguration> workloadConfigurations) {
        for (int i = 0; i < userCount; ++i) {
            new User(clock, this, time, findNodeWithMinThreadCount(workloadConfigurations), workloadConfigurations);
        }
    }

    public static NodeId findNodeWithMinThreadCount(LinkedHashMap<NodeId, WorkloadConfiguration> workloadConfigurations) {
        Iterator<Map.Entry<NodeId, WorkloadConfiguration>> it = workloadConfigurations.entrySet().iterator();
        Map.Entry<NodeId, WorkloadConfiguration> minNode = it.next();
        while (it.hasNext()) {
            Map.Entry<NodeId, WorkloadConfiguration> node = it.next();
            if (node.getValue().getThreads() < minNode.getValue().getThreads()) {
                minNode = node;
            }
        }
        return minNode.getKey();
    }

    public int getStartedUserCount() {
        return startedUserCount;
    }
}
