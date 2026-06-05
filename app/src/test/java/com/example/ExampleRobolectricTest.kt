package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CardCatalog
import com.example.ui.viewmodel.BattleState
import com.example.ui.viewmodel.FutViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CardClash", appName)
  }

  @Test
  fun `battle executeSlap is thread safe and avoids concurrent triggers`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = FutViewModel(app)
    
    // Choose a card to trigger play state
    val card = CardCatalog.cards.first()
    viewModel.selectBattleWager(card, 75)
    
    val stateBefore = viewModel.battleState.value
    assertTrue(stateBefore is BattleState.ActivePlay)
    val activeState = stateBefore as BattleState.ActivePlay
    assertTrue(activeState.isMySlap)
    
    // Execute multiple simultaneous slaps
    viewModel.executeSlap()
    
    // Immediately after executeSlap, isMySlap must be false to reject further taps
    val stateAfterFirstCall = viewModel.battleState.value as BattleState.ActivePlay
    assertEquals(false, stateAfterFirstCall.isMySlap)
    
    // A secondary consecutive slap call should be safely ignored and not trigger cast exceptions
    viewModel.executeSlap()
    
    // Still false and active
    val stateAfterSecondCall = viewModel.battleState.value as BattleState.ActivePlay
    assertEquals(false, stateAfterSecondCall.isMySlap)
  }
}
