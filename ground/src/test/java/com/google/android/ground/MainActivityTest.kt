/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.ground

import android.os.Looper
import com.google.android.ground.system.auth.SignInState
import com.google.common.truth.Truth.assertThat
import com.sharedtest.system.auth.FakeAuthenticationManager
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowProgressDialog

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class MainActivityTest : BaseHiltTest() {

  private lateinit var activity: MainActivity
  @Inject lateinit var fakeAuthenticationManager: FakeAuthenticationManager

  override fun setUp() {
    super.setUp()
    ShadowProgressDialog.reset()
  }

  @Test
  fun signInProgressDialog_whenSigningIn_isDisplayed() {
    Robolectric.buildActivity(MainActivity::class.java).use { controller ->
      controller.setup() // Moves Activity to RESUMED state
      activity = controller.get()

      fakeAuthenticationManager.setState(SignInState.signingIn())
      shadowOf(Looper.getMainLooper()).idle()

      assertThat(ShadowProgressDialog.getLatestDialog().isShowing).isTrue()
    }
  }

  @Test
  fun signInProgressDialog_whenSignedOutAfterSignInState_isNotDisplayed() {
    Robolectric.buildActivity(MainActivity::class.java).use { controller ->
      controller.setup() // Moves Activity to RESUMED state
      activity = controller.get()

      fakeAuthenticationManager.setState(SignInState.signingIn())
      fakeAuthenticationManager.setState(SignInState.signedOut())
      shadowOf(Looper.getMainLooper()).idle()

      assertThat(ShadowProgressDialog.getLatestDialog().isShowing).isFalse()
    }
  }
}
