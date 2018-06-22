package com.schibsted.account.ui.login

import android.content.Intent
import android.view.View
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.operation.ClientInfoOperation
import com.schibsted.account.ui.AccountUi
import com.schibsted.account.ui.login.flow.password.FlowSelectionListener
import com.schibsted.account.ui.navigation.DIALOG_SCREEN
import com.schibsted.account.ui.ui.dialog.LoadingDialogFragment
import kotlinx.android.synthetic.main.schacc_mobile_activity_layout.*
import java.lang.ref.WeakReference

class LoadClientInfoTask(activity: BaseLoginActivity, provider: InputProvider<Identifier>?) {

    private val ref: WeakReference<BaseLoginActivity> = WeakReference(activity)
    private lateinit var loadingDialogFragment: LoadingDialogFragment

    init {

        activity.supportFragmentManager.findFragmentByTag(DIALOG_SCREEN)?.let {
            loadingDialogFragment = it as LoadingDialogFragment
        } ?: activity.navigationController.navigationToDialog(LoadingDialogFragment())

        ClientInfoOperation({ error ->
            ref.get()?.let {
                it.setResult(AccountUi.RESULT_ERROR, Intent().putExtra(AccountUi.EXTRA_ERROR, error.toClientError()))
                it.finish()
            }
        }, { info ->
            ref.get()?.let {
                it.navigationController.dismissDialog(true)
                it.navigateToIdentificationFragment(info, (it as? FlowSelectionListener), provider)
                it.progressBar.visibility = View.GONE
                BaseLoginActivity.tracker?.merchantId = info.merchantId
            }
        })
    }
}
