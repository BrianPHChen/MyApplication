package com.example.myapplication

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Test Activity annotated with @AndroidEntryPoint for Hilt integration tests.
 * 
 * This activity is used by Compose test rules to ensure proper Hilt dependency injection
 * during instrumentation tests.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()