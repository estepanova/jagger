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

package com.griddynamics.jagger.coordinator.http;

import com.griddynamics.jagger.coordinator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

public abstract class AbstractProxyWorker extends ConfigurableWorker {
    private static final Logger log = LoggerFactory.getLogger(AbstractProxyWorker.class);

    private final Collection<Qualifier<Command<Serializable>>> qualifiers;

    public AbstractProxyWorker(Collection<Qualifier<Command<Serializable>>> qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    public void configure() {
        for (final Qualifier<Command<Serializable>> qualifier : qualifiers) {
            log.debug("Going to register proxy executor for qualifier {}", qualifier);
            onCommandReceived(qualifier.getClazz()).execute(
                    new CommandExecutor<Command<Serializable>, Serializable>() {
                        @Override
                        public Qualifier<Command<Serializable>> getQualifier() {
                            return qualifier;
                        }

                        @Override
                        public Serializable execute(Command<Serializable> command, NodeContext nodeContext) {
                            log.debug("Execution of command {} requested on node {}", command, nodeContext);

                            return handleCommand(command, nodeContext);
                        }
                    });
        }
    }

    protected abstract Serializable handleCommand(Command<Serializable> command, NodeContext nodeContext);
}
