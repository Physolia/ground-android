/*
 * Copyright 2021 Google LLC
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
package com.google.android.ground.repository

import com.google.android.ground.BaseHiltTest
import com.google.android.ground.domain.usecases.survey.ActivateSurveyUseCase
import com.google.common.truth.Truth.assertThat
import com.sharedtest.FakeData.JOB
import com.sharedtest.FakeData.SURVEY
import com.sharedtest.persistence.remote.FakeRemoteDataStore
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class SurveyRepositoryTest : BaseHiltTest() {
  @Inject lateinit var fakeRemoteDataStore: FakeRemoteDataStore
  @Inject lateinit var surveyRepository: SurveyRepository
  @Inject lateinit var activateSurvey: ActivateSurveyUseCase
  @Inject lateinit var testDispatcher: TestDispatcher

  @Test
  fun deleteSurvey_whenSurveyIsActive() =
    runTest(testDispatcher) {
      fakeRemoteDataStore.surveys = listOf(SURVEY)
      surveyRepository.syncSurveyWithRemote(SURVEY.id).await()
      advanceUntilIdle()
      activateSurvey(SURVEY.id)
      advanceUntilIdle()

      surveyRepository.removeOfflineSurvey(SURVEY.id)
      advanceUntilIdle()

      // Verify survey is deleted
      surveyRepository.offlineSurveys.test().assertValues(listOf())
      // Verify survey deactivated
      assertThat(surveyRepository.activeSurvey).isNull()
    }

  @Test
  fun deleteSurvey_whenSurveyIsInActive() =
    runTest(testDispatcher) {
      // Job ID must be globally unique.
      val job1 = JOB.copy(id = "job1")
      val job2 = JOB.copy(id = "job2")
      val survey1 = SURVEY.copy(id = "active survey id", jobMap = mapOf(job1.id to job1))
      val survey2 = SURVEY.copy(id = "inactive survey id", jobMap = mapOf(job2.id to job2))
      fakeRemoteDataStore.surveys = listOf(survey1, survey2)
      surveyRepository.syncSurveyWithRemote(survey1.id).await()
      surveyRepository.syncSurveyWithRemote(survey2.id).await()
      activateSurvey(survey1.id)
      advanceUntilIdle()

      surveyRepository.removeOfflineSurvey(survey2.id)
      advanceUntilIdle()

      // Verify active survey isn't cleared
      assertThat(surveyRepository.activeSurvey).isEqualTo(survey1)
      // Verify survey is deleted
      surveyRepository.offlineSurveys.test().assertValues(listOf(survey1))
    }
}
