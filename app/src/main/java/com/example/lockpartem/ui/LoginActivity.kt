package com.example.lockpartem.ui

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.lockpartem.R
import com.example.lockpartem.compoment.patternlockview.PatternViewStageState
import com.example.lockpartem.compoment.patternlockview.PatternViewState
import com.example.lockpartem.databinding.ActivityLoginBinding
import kotlinx.coroutines.flow.collect

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<PatternLockViewModel>()
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE)
        binding.patternLockView.stagePasswords[PatternViewStageState.FIRST] = sharedPreferences.getString("loginPassword", "").toString()
        binding.patternLockView.stageState = PatternViewStageState.SECOND

        binding.patternLockView.setOnChangeStateListener { state ->
            viewModel.updateViewState(state)
        }

        lifecycleScope.launchWhenCreated {
            viewModel.viewState.collect { patternViewState ->
                when(patternViewState){
                    is PatternViewState.Initial -> {
                        binding.patternLockView.reset()
                    }
                    is PatternViewState.Started -> {
                    }
                    is PatternViewState.Success -> {
                        if(binding.patternLockView.stageState != PatternViewStageState.FIRST)
                            Toast.makeText(this@LoginActivity, "Mở khóa thành công", Toast.LENGTH_LONG).show()
                    }
                    is PatternViewState.Error -> {
                        if(binding.patternLockView.stageState != PatternViewStageState.FIRST)
                            Toast.makeText(this@LoginActivity, "Mở khóa thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}