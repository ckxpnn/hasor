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
package test.net.demo.hasor;
import net.demo.hasor.core.DataSourceModule;
import net.demo.hasor.core.FreemarkerRender;
import net.demo.hasor.manager.oauth.TencentOAuth;
import net.hasor.core.ApiBinder;
import net.hasor.core.Module;
import net.hasor.restful.RenderEngine;
/**
 * 单元测试
 * @version : 2015年12月25日
 * @author 赵永春(zyc@hasor.net)
 */
public class UnitTestModule implements Module {
    @Override
    public void loadModule(ApiBinder apiBinder) throws Throwable {
        //
        apiBinder.installModule(new DataSourceModule());
        apiBinder.bindType(RenderEngine.class).uniqueName().toInstance(new FreemarkerRender());
        //
        // .Tencent
        TencentOAuth.configTencent(apiBinder);
    }
}