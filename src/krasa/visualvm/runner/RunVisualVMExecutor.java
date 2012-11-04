/*
 * This file is part of VisualVM for IDEA
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package krasa.visualvm.runner;

import com.intellij.execution.executors.DefaultRunExecutor;
import krasa.visualvm.Resources;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RunVisualVMExecutor extends DefaultRunExecutor {

    public static final String RUN_WITH_VISUAL_VM = "Run with VisualVM";
    public static final String RUN_WITH_VISUAL_VM1 = "RunWithVisualVM";

    @NotNull
    public String getToolWindowId() {
        return getId();
    }

    public Icon getToolWindowIcon() {
        return getIcon();
    }

    @NotNull
    public Icon getIcon() {
        return Resources.LOGO_16 ;
    }

    public Icon getDisabledIcon() {
        return null;
    }

    public String getDescription() {
        return RUN_WITH_VISUAL_VM;
    }

    @NotNull
    public String getActionName() {
        return RUN_WITH_VISUAL_VM1;
    }

    @NotNull
    public String getId() {
        return RUN_WITH_VISUAL_VM;
    }

    @NotNull
    public String getStartActionText() {
        return RUN_WITH_VISUAL_VM;
    }

    public String getContextActionId() {
        // HACK: ExecutorRegistryImpl expects this to be non-null, but we don't want any context actions for every file
        return getId() + " context-action-does-not-exist";
    }

    public String getHelpId() {
        return null;
    }
}
