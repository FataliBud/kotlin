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

package org.jetbrains.kotlin.java.model.impl

import com.intellij.psi.*
import com.intellij.psi.util.PsiTypesUtil
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.type.*
import java.lang.reflect.Array as RArray

abstract class JeTypeBase : TypeMirror {
    override fun getAnnotationMirrors() = emptyList<AnnotationMirror>()

    override fun <A : Annotation> getAnnotation(annotationType: Class<A>?) = null

    @Suppress("UNCHECKED_CAST")
    override fun <A : Annotation?> getAnnotationsByType(annotationType: Class<A>): Array<A> {
        return RArray.newInstance(annotationType, 0) as Array<A>
    }

    override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>, p: P) = v.visit(this, p)
}

abstract class JeAbstractType : JeTypeBase() {
    abstract val psi: PsiType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as JeAbstractType

        if (kind != other.kind) return false
        if (psi != other.psi) return false

        return true
    }

    override fun hashCode(): Int{
        var result = kind.hashCode()
        result = 31 * result + psi.hashCode()
        return result
    }
}

fun JeTypeMirror(psi: PsiType): TypeMirror = when (psi) {
    is PsiPrimitiveType -> JePrimitiveType(psi)
    PsiType.NULL -> JeNullType(psi)
    is PsiArrayType -> JeArrayType(psi)
    is PsiWildcardType -> JeWildcardType(psi)
    is PsiClassType -> JeDeclaredType(psi)
    is PsiIntersectionType -> JeIntersectionType(psi)
    else -> JeErrorType
}

private class JeNullType(override val psi: PsiType) : JeAbstractType(), NullType {
    override fun getKind() = TypeKind.NULL
}

private class JeArrayType(override val psi: PsiArrayType) : JeAbstractType(), ArrayType {
    override fun getKind() = TypeKind.ARRAY
    override fun getComponentType() = JeTypeMirror(psi.componentType)
}

private class JeWildcardType(override val psi: PsiWildcardType) : JeAbstractType(), WildcardType {
    override fun getKind() = TypeKind.WILDCARD
    override fun getSuperBound() = JeTypeMirror(psi.superBound)
    override fun getExtendsBound() = JeTypeMirror(psi.extendsBound)
}

private class JeIntersectionType(override val psi: PsiIntersectionType) : JeAbstractType(), IntersectionType {
    override fun getKind() = TypeKind.INTERSECTION
    override fun getBounds() = psi.superTypes.map { JeTypeMirror(it) }
}

class JeDeclaredType(override val psi: PsiClassType) : JeAbstractType(), DeclaredType {
    override fun getKind() = TypeKind.DECLARED
    
    override fun getTypeArguments() = psi.parameters.map { JeTypeMirror(it) }

    override fun asElement() = psi.resolve()?.let { JeTypeElement(it) }

    override fun getEnclosingType(): TypeMirror {
        val psiClass = psi.resolve()?.containingClass ?: return JeNoneType
        return JeTypeMirror(PsiTypesUtil.getClassType(psiClass))
    }
}

private class JePrimitiveType(override val psi: PsiPrimitiveType) : JeAbstractType(), PrimitiveType {
    override fun getKind() = when (psi) {
        PsiType.BYTE -> TypeKind.BYTE
        PsiType.CHAR -> TypeKind.CHAR
        PsiType.DOUBLE -> TypeKind.DOUBLE
        PsiType.FLOAT -> TypeKind.FLOAT
        PsiType.INT -> TypeKind.INT
        PsiType.LONG -> TypeKind.LONG
        PsiType.SHORT -> TypeKind.SHORT
        PsiType.BOOLEAN -> TypeKind.BOOLEAN
        PsiType.VOID -> TypeKind.VOID
        else -> TypeKind.ERROR
    }
}

class JeExecutableTypeMirror(val psi: PsiMember) : JeTypeBase(), TypeMirror {
    override fun getKind() = TypeKind.EXECUTABLE
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        return psi == (other as JeExecutableTypeMirror).psi
    }

    override fun hashCode(): Int {
        return psi.hashCode()
    }
}

object JePackageTypeMirror : JeTypeBase(), NoType {
    override fun getKind() = TypeKind.PACKAGE
    override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>, p: P) = v.visitNoType(this, p)
}

object JeNoneType : JeTypeBase(), NoType {
    override fun getKind() = TypeKind.NONE
    override fun <R : Any?, P : Any?> accept(v: TypeVisitor<R, P>, p: P) = v.visitNoType(this, p)
}

object JeErrorType : JeTypeBase(), NoType {
    override fun getKind() = TypeKind.ERROR
}

object JeDeclaredErrorType : JeTypeBase(), DeclaredType, NoType {
    override fun getKind() = TypeKind.ERROR

    override fun getTypeArguments() = emptyList<TypeMirror>()
    override fun asElement() = null
    override fun getEnclosingType() = JeNoneType
}