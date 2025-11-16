package com.girsang.server.service

import jakarta.persistence.EntityManager
import jakarta.persistence.metamodel.Attribute
import jakarta.persistence.OneToMany
import jakarta.persistence.ManyToMany
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import kotlin.reflect.KClass

@Service
open class EntityDeletionService(
    private val entityManager: EntityManager
) {

    /** Safe delete dengan pengecekan relasi */
    @Transactional
    open fun <T : Any> safeDelete(entityClass: Class<T>, id: Any) {
        val visited = mutableSetOf<Pair<Class<*>, Any>>()
        val entity = entityManager.find(entityClass, id)
            ?: throw RuntimeException("${entityClass.simpleName} dengan id $id tidak ditemukan")

        checkRelations(entity, visited)
        entityManager.remove(entity)
    }

    /** Rekursif cek relasi */
    private fun checkRelations(entity: Any, visited: MutableSet<Pair<Class<*>, Any>>) {
        val entityId = getEntityId(entity)
        val key = entity.javaClass to entityId
        if (visited.contains(key)) return
        visited.add(key)

        val entityType = entityManager.metamodel.entity(entity.javaClass)

        entityType.attributes.forEach { attr ->
            val field = entity.javaClass.declaredFields.firstOrNull { it.name == attr.name }
            field?.isAccessible = true

            when (attr.persistentAttributeType) {

                // OneToMany
                Attribute.PersistentAttributeType.ONE_TO_MANY -> {
                    val mappedBy = field?.getAnnotation(OneToMany::class.java)?.mappedBy
                    val targetEntity = field?.getAnnotation(OneToMany::class.java)?.targetEntity
                        ?: (attr.javaType as? Class<*>) ?: return
                    if (!mappedBy.isNullOrEmpty()) {
                        val entityName = getClassSimpleName(targetEntity)
                        val jpql = "SELECT COUNT(e) FROM $entityName e WHERE e.$mappedBy = :entity"
                        val count = entityManager.createQuery(jpql, java.lang.Long::class.java)
                            .setParameter("entity", entity)
                            .singleResult
                        if (count > 0) {
                            throw RuntimeException(
                                "${entity.javaClass.simpleName} masih digunakan di ${attr.name} (OneToMany) dengan $count referensi"
                            )
                        }
                    }
                }

                // OneToOne
                Attribute.PersistentAttributeType.ONE_TO_ONE -> {
                    val attrName = attr.name
                    val declaringClass = attr.declaringType.javaType ?: return
                    val entityName = getClassSimpleName(declaringClass)
                    val jpql = "SELECT COUNT(e) FROM $entityName e WHERE e.$attrName = :entity"
                    val count = entityManager.createQuery(jpql, java.lang.Long::class.java)
                        .setParameter("entity", entity)
                        .singleResult
                    if (count > 0) {
                        throw RuntimeException(
                            "${entity.javaClass.simpleName} masih direferensi di $attrName (OneToOne) dengan $count referensi"
                        )
                    }
                }

                // ManyToMany
                Attribute.PersistentAttributeType.MANY_TO_MANY -> {
                    val mappedBy = field?.getAnnotation(ManyToMany::class.java)?.mappedBy
                    val targetEntity = field?.getAnnotation(ManyToMany::class.java)?.targetEntity
                        ?: (attr.javaType as? Class<*>) ?: return
                    if (!mappedBy.isNullOrEmpty()) {
                        val entityName = getClassSimpleName(targetEntity)
                        val jpql = "SELECT COUNT(e) FROM $entityName e JOIN e.$mappedBy c WHERE c = :entity"
                        val count = entityManager.createQuery(jpql, java.lang.Long::class.java)
                            .setParameter("entity", entity)
                            .singleResult
                        if (count > 0) {
                            throw RuntimeException(
                                "${entity.javaClass.simpleName} masih digunakan di ${attr.name} (ManyToMany) dengan $count referensi"
                            )
                        }
                    }
                }

                else -> {
                    // Abaikan MANY_TO_ONE dan atribut lain
                }
            }
        }
    }

    /** Ambil ID entitas via reflection */
    private fun getEntityId(entity: Any): Any {
        val clazz = entity.javaClass
        val idField = clazz.declaredFields.firstOrNull { it.isAnnotationPresent(jakarta.persistence.Id::class.java) }
            ?: throw RuntimeException("Tidak ditemukan @Id di entitas ${clazz.simpleName}")
        idField.isAccessible = true
        return idField.get(entity)
    }

    /** Helper aman untuk ambil simpleName dari Class atau KClass */
    private fun getClassSimpleName(type: Any): String {
        return when (type) {
            is Class<*> -> type.simpleName ?: "Unknown"
            is KClass<*> -> type.simpleName ?: "Unknown"
            else -> "Unknown"
        }
    }
}
