/*
* JBoss, Home of Professional Open Source
* Copyright 2017 Red Hat Inc. and/or its affiliates and other contributors
* as indicated by the @authors tag. All rights reserved.
* See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.aesh.jvm.commands;

import org.aesh.command.Command;
import org.aesh.command.CommandDefinition;
import org.aesh.command.CommandException;
import org.aesh.command.CommandResult;
import org.aesh.command.invocation.CommandInvocation;
import org.aesh.command.option.Option;
import org.aesh.jvm.JVM;
import org.aesh.jvm.utils.JVMProcesses;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
 */
@CommandDefinition(name ="jps", description = "List the instrumented Java Virtual Machines (JVMs) on the target system.")
public class Jps implements Command {

    @Option(name = "quiet", shortName = 'q', hasValue = false,
            description = "Suppresses the output of the class name, Jar file name and arguments passed to the main method,"+
                    " producing only a list of local JVM identifiers")
    private boolean quiet;

    @Option(name = "main", shortName = 'm', hasValue = false,
            description = "Display the arguments passed to the main method. The output may be null for embedded JVMs.")
    private boolean main;

    @Option(name = "list", shortName = 'l', hasValue = false,
    description = "Displays the full package name for the application's main class or the full path name to the "+
            "application's JAR file.")
    private boolean list;

    @Option(name = "verbose", shortName = 'v', hasValue = false,
            description = "Displays the arguments passed to the JVM.")
    private boolean verbose;


    @Override
    public CommandResult execute(CommandInvocation commandInvocation) throws CommandException, InterruptedException {

        if(JVMProcesses.canAttachToLocalJVMs()) {

            if(quiet) {
                List<Integer> jvms = JVMProcesses.getMonitoredVMIds();
                for(Integer id : jvms)
                    commandInvocation.println(String.valueOf(id));
            }
            else if(main) {
                Map<Integer, JVM> jvms = JVMProcesses.getAllVirtualMachines();
                for(Integer id : jvms.keySet()) {
                    JVM jvm = jvms.get(id);
                    if(jvm.commandLine().indexOf(' ') > 0)
                        commandInvocation.println(jvm.vmId()+" "+jvm.name()+" "+jvm.commandLine());
                    else
                        commandInvocation.println(jvm.vmId()+" "+jvm.name());
                }
            }
            else if(list) {
                Map<Integer, JVM> jvms = JVMProcesses.getAllVirtualMachines();
                for(Integer id : jvms.keySet()) {
                    JVM jvm = jvms.get(id);
                    String cmd = jvm.commandLine();
                    if(cmd.indexOf(' ') > 0)
                        cmd = cmd.substring(0, cmd.indexOf(' '));
                    commandInvocation.println(jvm.vmId()+" "+cmd);
                }
            }
            else if(verbose) {
                Map<Integer, JVM> jvms = JVMProcesses.getAllVirtualMachines();
                for(Integer id : jvms.keySet()) {
                    JVM jvm = jvms.get(id);
                    commandInvocation.println(jvm.vmId()+" "+jvm.name()+" "+jvm.flags());
                }
            }
            else {
                Map<Integer, JVM> jvms = JVMProcesses.getAllVirtualMachines();
                for(Integer id : jvms.keySet()) {
                    JVM jvm = jvms.get(id);
                    commandInvocation.println(jvm.vmId()+" "+jvm.name());
                }
            }

            return CommandResult.SUCCESS;
        }
        else
            commandInvocation.println("Jps cannot list any running JVMs are you running with OpenJDK/OracleJDK and have tools.jar in your classpath?");

        return CommandResult.SUCCESS;
    }

}
