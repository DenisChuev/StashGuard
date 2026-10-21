package dc.stashguard.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * Base class for ViewModel tests: `viewModelScope` runs on `Dispatchers.Main`, which is replaced by
 * an unconfined test dispatcher, so launched work runs immediately and `runTest` shares its scheduler.
 * Create ViewModels inside the tests, not in property initializers, which run before [setMainDispatcher].
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainDispatcherTest {
    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }
}
