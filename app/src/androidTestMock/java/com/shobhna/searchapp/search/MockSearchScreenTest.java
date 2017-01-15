package com.shobhna.searchapp.search;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.core.deps.guava.collect.Lists;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.widget.EditText;

import com.shobhna.searchapp.MainActivity;
import com.shobhna.searchapp.R;
import com.shobhna.searchapp.model.FakeSearchServiceApiImpl;
import com.google.api.services.customsearch.model.Result;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class MockSearchScreenTest {

    private static Result result1 = new Result();

    private static Result result2 = new Result();

    private static List<Result> RESULTS;

    private String mSearchQueryString = "Batman";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true /* Initial touch mode  */,
                    false /* Lazily launch activity */);

    @Before
    public void intentWithStubbedNoteId() {
        result1.setTitle("Result 1");
        result1.setDisplayLink("Display link 1");
        result1.setSnippet("Snippet 1");

        result2.setTitle("Result 2");
        result2.setDisplayLink("Display link 2");
        result2.setSnippet("Snippet 2");

        RESULTS = Lists.newArrayList(result1, result2);
        FakeSearchServiceApiImpl.setResultList(RESULTS);
        // Lazily start the Activity from the ActivityTestRule this time to inject the start Intent
        Intent startIntent = new Intent();
        mActivityTestRule.launchActivity(startIntent);
        registerIdlingResource();
    }

    @Test
    public void checkSearchScreenUIComponents() {
        // Verify proper tabs are visible on screen
        onView(withText(R.string.image_search)).check(matches(isDisplayed()));
        onView(withText(R.string.web_search)).check(matches(isDisplayed()));
        // Verify search view is visible
        onView(withId(R.id.search_view)).check(matches(isDisplayed()));
        // Verify No Result image is visible
        onView(withId(R.id.no_result_image)).check(matches(isDisplayed()));
    }

    @Test
    public void enterQueryString_showResults() {
        // Check page change view is not visible at beginning
        onView(withId(R.id.page_change_web_search_view)).check(matches(not(isDisplayed())));
        // Check recycler view is not visible at beginning
        onView(withId(R.id.web_search_list)).check(matches(not(isDisplayed())));

        // Enter search string and close the keyboard
        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isAssignableFrom(EditText.class))).perform(typeText(mSearchQueryString), pressKey(66), closeSoftKeyboard());

        // Check if recycler view is visible
        onView(withId(R.id.web_search_list)).check(matches(isDisplayed()));
        // Check if page change view is visible
        onView(withId(R.id.page_change_web_search_view)).check(matches(isDisplayed()));

        // Check string of the result items
        onView(withId(R.id.web_search_list))
                .perform(scrollTo(hasDescendant(withText(RESULTS.get(0).getTitle()))));
        onView(withId(R.id.web_search_list))
                .perform(scrollTo(hasDescendant(withText(RESULTS.get(1).getTitle()))));
    }


    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(
                mActivityTestRule.getActivity().getCountingIdlingResource());
    }

    private void registerIdlingResource() {
        Espresso.registerIdlingResources(
                mActivityTestRule.getActivity().getCountingIdlingResource());
    }
}
