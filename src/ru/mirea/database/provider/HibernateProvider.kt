package ru.mirea.database.provider

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import ru.mirea.database.interfaces.DatabaseProviderInterface
import ru.mirea.database.schema.Settings

class HibernateProvider : DatabaseProviderInterface {
    private var sessionFactory: SessionFactory? = null
    private var registry: StandardServiceRegistry? = null

    fun setupSession(): HibernateProvider {
        val configuration = Configuration()

        configuration.addAnnotatedClass(Settings::class.java)

        configuration.configure("hibernate.cfg.xml")

        registry = StandardServiceRegistryBuilder().applySettings(configuration.properties!!).build()

        try {
            sessionFactory = configuration.buildSessionFactory(registry)
        } catch (ex: Exception) {
            System.err.println(ex.message)
            if (sessionFactory != null)
                StandardServiceRegistryBuilder.destroy(registry)
        }

        return this
    }

    fun closeSession(): HibernateProvider {
        if (sessionFactory != null && sessionFactory!!.isOpen) {
            sessionFactory!!.close()
        }
        return this
    }

    override fun <T : Any> add(classRef: T): Boolean {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setupSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()
            session.save(classRef)
            session.transaction.commit()
            session.close()
            true
        } catch (ex: Exception) {
            session?.close()
            false
        }
    }

    override fun <T : Any> delete(classRef: T): Boolean {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setupSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()
            session.delete(classRef)
            session.transaction.commit()
            session.close()

            true
        } catch (ex: Exception) {
            session?.close()
            false
        }
    }

    override fun <T : Any> get(classRef: T): List<T> {
        var session: Session? = null

        if (sessionFactory == null || sessionFactory!!.isClosed)
            setupSession()

        return try {
            session = sessionFactory!!.openSession()
            session.beginTransaction()

            val builder = session.criteriaBuilder
            val criteria = builder.createQuery(classRef::class.java)
            criteria.from(classRef::class.java)
            val userProperties = session.createQuery(criteria).resultList

            session.close()
            userProperties
        } catch (ex: Exception) {
            session?.close()
            mutableListOf()
        }
    }

}