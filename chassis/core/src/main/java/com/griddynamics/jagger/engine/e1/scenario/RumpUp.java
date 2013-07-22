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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * @author Nikolay Musienko
 *         Date: 01.07.13
 */
public class RumpUp implements FunctionOfTime {
    private final Logger log = LoggerFactory.getLogger(RumpUp.class);

    private final BigDecimal value;
    private long warmUpTime;
    private long startTime = -1;
    private long deferredStart;
    private String name;
    private BigDecimal k;

    public RumpUp(BigDecimal value, long warmUpTime) {
        this(value, warmUpTime, "");
    }

    public RumpUp(BigDecimal value, long warmUpTime, String name) {
        this(value, warmUpTime, 0, name);
    }

    public RumpUp(BigDecimal value, long warmUpTime, long deferredStart, String name) {
        this.name = name;
        this.value = value;
        this.warmUpTime = warmUpTime;
        this.deferredStart = deferredStart;
    }

    @Override
    public BigDecimal get(long time) {
        if(startTime == -1) {
            startTime = time + deferredStart;
            warmUpTime += startTime;
            k = value.divide(new BigDecimal(warmUpTime - startTime));
        }
        if (time > warmUpTime) {
            return value;
        }
        if (time < startTime) {
            return BigDecimal.ZERO;
        }
        BigDecimal currentValue = k.multiply(new BigDecimal(time - startTime));
        log.debug("Changing rate up to: {}", currentValue);
        return currentValue;
    }

    @Override
    public BigDecimal getDisplayValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RumpUp" + name + " {" +
                ", " + name + "=" + value +
                ", warmUpTime=" + warmUpTime +
                ", startTime=" + startTime +
                '}';
    }
}
