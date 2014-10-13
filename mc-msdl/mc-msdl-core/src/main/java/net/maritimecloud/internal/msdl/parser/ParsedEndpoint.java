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
package net.maritimecloud.internal.msdl.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import net.maritimecloud.internal.msdl.parser.antlr.MsdlParser.EndpointDeclarationContext;
import net.maritimecloud.internal.msdl.parser.antlr.MsdlParser.FunctionContext;
import net.maritimecloud.msdl.model.EndpointDefinition;
import net.maritimecloud.msdl.model.EndpointMethod;

/**
 *
 * @author Kasper Nielsen
 */
public class ParsedEndpoint extends AbstractContainer implements EndpointDefinition {

    final LinkedHashMap<String, ParsedEndpointFunction> endpointFunction = new LinkedHashMap<>();

    ParsedEndpoint(ParsedMsdlFile file, AnnotationContainer ac) {
        super(file, ac);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<EndpointMethod> getFunctions() {
        return (List) Collections.unmodifiableList(new ArrayList<>(endpointFunction.values()));
    }

    /**
     * @param child
     */
    ParsedEndpoint parse(EndpointDeclarationContext c) {
        setName(c.Identifier().getText());
        for (FunctionContext ec : c.function()) {
            ParsedEndpointFunction pf = new ParsedEndpointFunction(this);
            pf.parse(ec);
            if (pf != null) {
                if (endpointFunction.containsKey(pf.name)) {
                    file.error(ec, "variable name '" + pf.name + "' is defined multiple times in the message '"
                            + pf.name + "'");
                } else {
                    endpointFunction.put(pf.name, pf);
                }
            }
        }
        return this;
    }
}