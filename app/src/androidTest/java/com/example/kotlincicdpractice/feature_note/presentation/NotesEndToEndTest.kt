package com.example.kotlincicdpractice.feature_note.presentation

import androidx.activity.compose.setContent
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kotlincicdpractice.core.util.TestTags
import com.example.kotlincicdpractice.di.AppModule
import com.example.kotlincicdpractice.feature_note.presentation.add_edit_note.AddEditNoteScreen
import com.example.kotlincicdpractice.feature_note.presentation.notes.NotesScreen
import com.example.kotlincicdpractice.feature_note.presentation.util.Screen
import com.example.kotlincicdpractice.ui.theme.KotlinCICDPracticeTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class)
class NotesEndToEndTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        composeRule.activity.setContent {
            KotlinCICDPracticeTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.NotesScreen.route,
                ) {
                    composable(route = Screen.NotesScreen.route) {
                        NotesScreen(navController = navController)
                    }
                    composable(
                        route = Screen.AddEditNoteScreen.route + "?noteId={noteId}&noteColor={noteColor}",
                        arguments =
                            listOf(
                                navArgument(
                                    name = "noteId",
                                    builder = {
                                        type = NavType.IntType
                                        defaultValue = -1
                                    },
                                ),
                                navArgument(
                                    name = "noteColor",
                                    builder = {
                                        type = NavType.IntType
                                        defaultValue = -1
                                    },
                                ),
                            ),
                    ) {
                        val color = it.arguments?.getInt("noteColor") ?: -1
                        AddEditNoteScreen(navController = navController, noteColor = color)
                    }
                }
            }
        }
    }

    @Test
    fun saveNewNote_editAfterwards() {
        // Click on FAB to get to add note screen
        composeRule.onNodeWithContentDescription("Add").performClick()

        // Enter texts in title and content text fields
        composeRule.onNodeWithTag(TestTags.TITLE_TEXT_FIELD).performTextInput("Test-title")
        composeRule.onNodeWithTag(TestTags.CONTENT_TEXT_FIELD).performTextInput("Test-content")
        // Save the new note
        composeRule.onNodeWithContentDescription("Save").performClick()

        // Make sure there is a note in the list with our title and content
        composeRule.onNodeWithText("Test-title").assertIsDisplayed()
        composeRule.onNodeWithText("Test-content").assertIsDisplayed()
        // Click on note to edit it
        composeRule.onNodeWithText("Test-title").performClick()

        // Make sure title and content text fields contain note title and content
        composeRule.onNodeWithTag(TestTags.TITLE_TEXT_FIELD).assertTextContains("Test-title")
        composeRule.onNodeWithTag(TestTags.CONTENT_TEXT_FIELD).assertTextContains("Test-content")

        // Add the text "2" to the title and content text fields
        composeRule.onNodeWithTag(TestTags.TITLE_TEXT_FIELD).performTextInput("2")
        composeRule.onNodeWithTag(TestTags.CONTENT_TEXT_FIELD).performTextInput("2")
        // Update the note
        composeRule.onNodeWithContentDescription("Save").performClick()

        // Make sure the update was applied to the note in the list
        composeRule.onNodeWithText("2Test-title").assertIsDisplayed()
        composeRule.onNodeWithText("2Test-content").assertIsDisplayed()
    }

    @Test
    fun saveNewNotes_orderByTitleDescending() {
        for (i in 1..3) {
            // Click on FAB to get to add note screen
            composeRule.onNodeWithContentDescription("Add").performClick()

            // Enter texts in title and content text fields
            composeRule.onNodeWithTag(TestTags.TITLE_TEXT_FIELD).performTextInput(i.toString())
            composeRule.onNodeWithTag(TestTags.CONTENT_TEXT_FIELD).performTextInput(i.toString())
            // Save the new note
            composeRule.onNodeWithContentDescription("Save").performClick()
        }

        composeRule.onNodeWithText("1").assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Sort").performClick()
        composeRule.onNodeWithContentDescription("Title").performClick()
        composeRule.onNodeWithContentDescription("Descending").performClick()

        composeRule.onAllNodesWithTag(TestTags.NOTE_LIST_ITEM)[0].assertTextContains("3")
        composeRule.onAllNodesWithTag(TestTags.NOTE_LIST_ITEM)[1].assertTextContains("2")
        composeRule.onAllNodesWithTag(TestTags.NOTE_LIST_ITEM)[2].assertTextContains("1")
    }

    @Test
    fun saveNewNotes_deleteAny_undo_isVisible() {
        for (i in 1..3) {
            // Click on FAB to get to add note screen
            composeRule.onNodeWithContentDescription("Add").performClick()

            // Enter texts in title and content text fields
            composeRule.onNodeWithTag(TestTags.TITLE_TEXT_FIELD).performTextInput(i.toString())
            composeRule.onNodeWithTag(TestTags.CONTENT_TEXT_FIELD).performTextInput(i.toString())
            // Save the new note
            composeRule.onNodeWithContentDescription("Save").performClick()
        }

        composeRule.onNodeWithText("1").assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()
        composeRule.onNodeWithText("3").assertIsDisplayed()

        getItem(TestTags.NOTE_LIST, "3").onChildren().filterToOne(
            hasAnyDescendant(
                hasContentDescription("Delete"),
            ),
        ).performClick()

        composeRule.onNodeWithText("3").assertIsNotDisplayed()

        composeRule.onNodeWithText("Note deleted").assertIsDisplayed()
        composeRule.onNodeWithText("Undo").assertIsDisplayed()

        composeRule.onNodeWithText("Undo").performClick()

        composeRule.onNodeWithText("3").assertIsDisplayed()
    }

    private fun getItem(
        tag: String,
        name: String,
    ): SemanticsNodeInteraction {
        return composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .performScrollToNode(hasAnyDescendant(hasText(name)))
            .onChildren()
            .filterToOne(hasAnyDescendant(hasText(name)))
    }
}
