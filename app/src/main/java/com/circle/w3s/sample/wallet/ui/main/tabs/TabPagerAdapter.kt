// Copyright (c) 2024, Circle Internet Financial, LTD. All rights reserved.
//
// SPDX-License-Identifier: Apache-2.0
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.circle.w3s.sample.wallet.ui.main.tabs

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = TAB_COUNT

    override fun createFragment(position: Int): Fragment = tabFragmentFor(position)

    companion object {
        const val TAB_COUNT = 3

        // Set on the ViewPager2 so every tab's view stays attached. Pinned next to TAB_COUNT
        // so the two stay in lock-step; the mapping test asserts the relationship.
        const val OFFSCREEN_PAGE_LIMIT_FOR_FULL_RESIDENCY = TAB_COUNT - 1

        // Titles live next to the mapping so position 0/1/2 → Social/Email/PIN stays in lock-step.
        // TabLayoutMediator in MainActivity reads these by index.
        val TAB_TITLES: List<String> = listOf("Social", "Email", "PIN")

        // Fails fast on an unexpected position rather than silently substituting a tab.
        // getItemCount() pins the valid range to 0..TAB_COUNT-1.
        internal fun tabFragmentFor(position: Int): Fragment = when (position) {
            0 -> TabPageSocialFragment()
            1 -> TabPageEmailFragment()
            2 -> TabPagePinFragment()
            else -> error("Unexpected tab position: $position")
        }
    }
}
