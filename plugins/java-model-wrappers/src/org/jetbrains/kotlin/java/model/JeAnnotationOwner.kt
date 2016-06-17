/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.java.model

import com.intellij.psi.PsiAnnotationOwner
import org.jetbrains.kotlin.java.model.impl.JeAnnotationMirror
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.Opcodes.*
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import java.lang.reflect.Array as RArray

interface JeAnnotationOwner : Element {
    val annotationOwner: PsiAnnotationOwner?
    
    override fun getAnnotationMirrors() = annotationOwner?.annotations?.map { JeAnnotationMirror(it) } ?: emptyList()
    
    override fun <A : Annotation> getAnnotation(annotationType: Class<A>): A? {
        val annotationFqName = annotationType.canonicalName
        val annotation = annotationOwner?.annotations
                ?.firstOrNull { it.qualifiedName == annotationFqName } ?: return null

        val bytes = with (ClassWriter(0)) {
            visit(49, ACC_PUBLIC, annotationFqName,
                  null, null, arrayOf("java/lang/annotation/Annotation"))
            visitSource(null, null)
            visitEnd()
            toByteArray()
        }
        
        return null
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A> {
        return RArray.newInstance(annotationType, 0) as Array<A>
    }
}

interface JeNoAnnotations : Element {
    override fun getAnnotationMirrors() = emptyList<AnnotationMirror>()

    override fun <A : Annotation> getAnnotation(annotationType: Class<A>?) = null

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A> {
        return RArray.newInstance(annotationType, 0) as Array<A>
    }
}