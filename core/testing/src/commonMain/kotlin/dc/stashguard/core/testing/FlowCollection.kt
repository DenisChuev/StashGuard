package dc.stashguard.core.testing

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Collects [flow] for the rest of the test, as a screen does, so a `stateIn(WhileSubscribed)`
 * flow stays active and its `value` is up to date. Collection starts immediately.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.keepCollecting(flow: Flow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flow.collect() }
}
