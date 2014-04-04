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
package net.maritimecloud.msdl.parser;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.maritimecloud.msdl.model.EnumDeclaration;
import net.maritimecloud.msdl.model.FileDeclaration;
import net.maritimecloud.msdl.model.MessageDeclaration;
import net.maritimecloud.msdl.parser.antlr.AntlrFile;
import net.maritimecloud.msdl.parser.antlr.MsdlParser.EnumDeclarationContext;
import net.maritimecloud.msdl.parser.antlr.MsdlParser.ImportDeclarationContext;
import net.maritimecloud.msdl.parser.antlr.MsdlParser.MessageDeclarationContext;
import net.maritimecloud.msdl.parser.antlr.MsdlParser.NamespaceDeclarationContext;
import net.maritimecloud.msdl.parser.antlr.MsdlParser.ServiceDeclarationContext;
import net.maritimecloud.msdl.parser.antlr.MsdlParser.TypeDeclarationContext;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 *
 * @author Kasper Nielsen
 */
class ParsedFile implements FileDeclaration {
    final AntlrFile antlrFile;

    final ArrayList<String> imports = new ArrayList<>();

    String namespace;

    final ParsedProject project;

    final ArrayList<ParsedFile> resolvedImports = new ArrayList<>();

    final List<ParsedEnum> enums = new ArrayList<>();

    final List<ParsedMessage> messages = new ArrayList<>();

    ParsedFile(ParsedProject project, AntlrFile antlrFile) {
        this.antlrFile = requireNonNull(antlrFile);
        this.project = requireNonNull(project);
    }

    void parse() {
        parseNamespace();
        parseImports();
        parseTypes();
        // return f;
    }

    private void parseImports() {
        for (ImportDeclarationContext importContext : antlrFile.getCompilationUnit().importDeclaration()) {
            String p = importContext.getChild(1).getText();
            p = p.substring(1, p.length() - 1);
            imports.add(p);
        }
    }

    private void parseNamespace() {
        NamespaceDeclarationContext namespaceContext = antlrFile.getCompilationUnit().namespaceDeclaration();
        // namespace is optional
        if (namespaceContext != null) {
            namespace = namespaceContext.getChild(1).getText();
            // TODO validate name spaces
        }
    }

    private void parseTypes() {
        for (TypeDeclarationContext tdc : antlrFile.getCompilationUnit().typeDeclaration()) {
            AnnotationContainer ac = new AnnotationContainer(this).parse(tdc);

            ParseTree child = tdc.getChild(tdc.getChildCount() - 1);
            if (child instanceof EnumDeclarationContext) {
                enums.add(new ParsedEnum(this, ac).parse((EnumDeclarationContext) child));
            } else if (child instanceof MessageDeclarationContext) {
                messages.add(new ParsedMessage(this, ac).parse((MessageDeclarationContext) child));
            } else if (child instanceof ServiceDeclarationContext) {
                // types.add(ParsedService.create(this, (ServiceDeclarationContext) child));
            }
        }
    }

    void error(ParserRuleContext context, String msg) {
        String st = antlrFile.getPath() + ":[" + context.getStart().getLine() + ":"
                + context.getStart().getCharPositionInLine() + "] ";
        st += msg;
        project.logger.error(st);
    }

    void warn(ParserRuleContext context, String msg) {}

    /** {@inheritDoc} */
    @Override
    public String getNamespace() {
        return namespace;
    }

    /** {@inheritDoc} */
    @Override
    public Path getPath() {
        return antlrFile.getPath();
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<EnumDeclaration> getEnums() {
        return (List) Collections.unmodifiableList(enums);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<MessageDeclaration> getMessages() {
        return (List) Collections.unmodifiableList(messages);
    }
}
