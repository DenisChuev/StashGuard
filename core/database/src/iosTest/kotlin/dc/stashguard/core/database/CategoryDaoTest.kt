package dc.stashguard.core.database

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CategoryDaoTest {
    private val database = inMemoryDatabase()
    private val dao = database.getCategoryDao()

    @AfterTest
    fun closeDatabase() = database.close()

    @Test
    fun categoriesAreSortedByName() = runTest {
        dao.insertCategories(listOf(categoryEntity("1", "Rent"), categoryEntity("2", "Food"), categoryEntity("3", "Salary")))

        assertEquals(listOf("Food", "Rent", "Salary"), dao.getAllCategories().first().map { it.name })
    }

    @Test
    fun categoriesByTypeIncludeBothTypeCategories() = runTest {
        dao.insertCategories(
            listOf(
                categoryEntity("food", "Food", type = "EXPENSE"),
                categoryEntity("salary", "Salary", type = "REVENUE"),
                categoryEntity("transfer", "Transfer", type = "BOTH"),
            )
        )

        assertEquals(listOf("food", "transfer"), dao.getCategoriesByType("EXPENSE").first().map { it.id })
        assertEquals(listOf("salary", "transfer"), dao.getCategoriesByType("REVENUE").first().map { it.id })
    }

    @Test
    fun updateAndDeleteChangeOnlyThatCategory() = runTest {
        dao.insertCategories(listOf(categoryEntity("food", "Food"), categoryEntity("rent", "Rent")))

        dao.updateCategory(categoryEntity("food", "Groceries"))
        dao.deleteCategoryById("rent")

        assertEquals("Groceries", dao.getCategoryById("food")?.name)
        assertNull(dao.getCategoryById("rent"))
    }
}
