/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hasor.view.decorate.support;
import org.hasor.binder.ApiBinder;
import org.hasor.context.AppContext;
import org.hasor.context.PlatformListener;
import org.hasor.startup.PlatformExt;
import org.hasor.view.decorate.DecorateBinder;
import org.hasor.view.decorate.parser.FreemarkerHtmlParser;
/**
 * 装饰服务，启动级别L0
 * @version : 2013-4-8
 * @author 赵永春 (zyc@byshell.org)
 */
@PlatformExt(displayName = "DecoratePlatformListener", description = "org.platform.view.decorate软件包功能支持。", startIndex = PlatformExt.Lv_0)
public class DecoratePlatformListener implements PlatformListener {
    @Override
    public void initialize(ApiBinder binder) {
        binder.getGuiceBinder().bind(DecorateBinder.class).to(DecorateBinderImplements.class);
        binder.filter("*").through(ManagedDecorateFilter.class);
        //
        DecorateBinderImplements decorateBinder = new DecorateBinderImplements();
        decorateBinder.decFilter("text/html;charset=UTF-8", "*").through(FreemarkerHtmlParser.class);
        decorateBinder.configure(binder.getGuiceBinder());
    }
    @Override
    public void initialized(AppContext appContext) {
        // TODO Auto-generated method stub
    }
    @Override
    public void destroy(AppContext appContext) {
        // TODO Auto-generated method stub
    }
}