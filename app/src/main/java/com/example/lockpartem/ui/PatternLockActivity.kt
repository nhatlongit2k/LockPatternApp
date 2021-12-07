package com.example.lockpartem.ui

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.lockpartem.R
import com.example.lockpartem.compoment.patternlockview.PatternViewStageState
import com.example.lockpartem.compoment.patternlockview.PatternViewState
import com.example.lockpartem.databinding.ActivityPatternLockBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PatternLockActivity : AppCompatActivity() {
    private val viewModel by viewModels<PatternLockViewModel>()

    private lateinit var binding: ActivityPatternLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPatternLockBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val sharedPreferences: SharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE)
        if(sharedPreferences.getString("loginPassword", "").toString().equals("")==false){
            val intent = Intent(this@PatternLockActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }else{
            binding.clearTextButton.setOnClickListener {
                viewModel.updateViewState(PatternViewState.Initial)
            }

            binding.stageButton.setOnClickListener {
                when (binding.patternLockView.stageState) {
                    PatternViewStageState.FIRST -> {
                        viewModel.updateViewState(PatternViewState.Initial)

                        binding.patternLockView.stageState = PatternViewStageState.SECOND
                        binding.stageButton.text = getString(R.string.stage_button_confirm)
                        binding.tvSubTitle.isInvisible = true
                    }
                    PatternViewStageState.SECOND -> {
                        AlertDialog.Builder(this).apply {
                            setMessage(R.string.alert_dialog_confirm_message)
                            setPositiveButton(
                                R.string.alert_dialog_positive_button, object : DialogInterface.OnClickListener{
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        Log.d("TAG", "onClick: ${binding.patternLockView.stagePasswords[PatternViewStageState.SECOND].toString()}")
                                        val sharedPreferences: SharedPreferences = getSharedPreferences("dataLogin", MODE_PRIVATE)
                                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                        editor.putString("loginPassword",binding.patternLockView.stagePasswords[PatternViewStageState.SECOND].toString())
                                        editor.commit()
                                        val intent: Intent = Intent(this@PatternLockActivity, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                }
                            )
                        }.show()
                    }
                }
            }

            binding.patternLockView.setOnChangeStateListener { state ->
                viewModel.updateViewState(state)
            }

            lifecycleScope.launchWhenCreated {
                viewModel.viewState.collect { patternViewState ->
                    when(patternViewState){
                        is PatternViewState.Initial->{
                            binding.patternLockView.reset()
                            binding.stageButton.isEnabled = false
                            binding.clearTextButton.isVisible = false
                            binding.tvMessage.run {
                                text = if (binding.patternLockView.stageState == PatternViewStageState.FIRST) {
                                    getString(R.string.initial_message_first_stage)
                                } else {
                                    getString(R.string.initial_message_second_stage)
                                }
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.message_text_default_color
                                    )
                                )
                            }
                        }
                        is PatternViewState.Started -> {
                            binding.tvMessage.run {
                                text = getString(R.string.started_message)
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.message_text_default_color
                                    )
                                )
                            }
                        }
                        is PatternViewState.Success -> {
                            binding.stageButton.isEnabled = true
                            binding.tvMessage.run {
                                text = if (binding.patternLockView.stageState == PatternViewStageState.FIRST) {
                                    getString(R.string.success_message_first_stage)
                                } else {
                                    getString(R.string.success_message_second_stage)
                                }
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.message_text_default_color
                                    )
                                )
                            }
                            binding.clearTextButton.isVisible =
                                binding.patternLockView.stageState == PatternViewStageState.FIRST
                        }
                        is PatternViewState.Error -> {
                            binding.tvMessage.run {
                                text = if (binding.patternLockView.stageState == PatternViewStageState.FIRST) {
                                    getString(R.string.error_message_first_stage)
                                } else {
                                    getString(R.string.error_message_second_stage)
                                }
                                setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        R.color.message_text_error_color
                                    )
                                )
                            }
                            binding.clearTextButton.isVisible =
                                binding.patternLockView.stageState == PatternViewStageState.FIRST
                        }
                    }
                }
            }
        }
    }
}