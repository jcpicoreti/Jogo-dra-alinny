package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CharacterId
import com.example.data.model.EvolutionType
import com.example.data.model.StageId
import com.example.data.model.WeaponType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dra Alinny vs os germes do mal", appName)
  }

  @Test
  fun `verify character list includes all requested heroes`() {
    assertNotNull(CharacterId.DRA_ALINNY)
    assertNotNull(CharacterId.LORENZO)
    assertNotNull(CharacterId.RAYNARA)
    assertNotNull(CharacterId.JULIO)
    assertNotNull(CharacterId.RICK)
    assertNotNull(CharacterId.BART)
    assertEquals(6, CharacterId.values().size)
  }

  @Test
  fun `verify weapon evolutions configured`() {
    assertEquals(EvolutionType.ESCOVA_ULTRASSONICA, WeaponType.ESCOVA_DENTE.evolvedWeapon)
    assertEquals(EvolutionType.FIO_LASER_TITANIO, WeaponType.FIO_DENTAL.evolvedWeapon)
  }

  @Test
  fun `verify stages configured`() {
    assertEquals(6, StageId.values().size)
  }
}
