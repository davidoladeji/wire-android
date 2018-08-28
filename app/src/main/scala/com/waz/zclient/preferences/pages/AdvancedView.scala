/**
 * Wire
 * Copyright (C) 2018 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.preferences.pages

import android.app.Activity
import android.content.{Context, Intent}
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.{LinearLayout, Toast}
import com.waz.service.ZMessaging
import com.waz.threading.{CancellableFuture, Threading}
import com.waz.utils.returning
import com.waz.zclient.preferences.views.TextButton
import com.waz.zclient.utils.ContextUtils._
import com.waz.zclient.utils.{BackStackKey, DebugUtils}
import com.waz.zclient.{R, ViewHelper}

import scala.concurrent.duration._
trait AdvancedView

class AdvancedViewImpl(context: Context, attrs: AttributeSet, style: Int) extends LinearLayout(context, attrs, style) with AdvancedView with ViewHelper {
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null, 0)

  inflate(R.layout.preferences_advanced_layout)

  val submitReport = returning(findById[TextButton](R.id.preferences_debug_report)) { button =>
    button.onClickEvent { _ =>
      DebugUtils.sendDebugReport(context.asInstanceOf[Activity])
    }
  }

  val resetPush = returning(findById[TextButton](R.id.preferences_reset_push)) { button =>
    def setResetEnabled(enabled: Boolean) = {
      button.setEnabled(enabled)
      button.setAlpha(if (enabled) 1.0f else 0.5f)
    }

    button.onClickEvent { _ =>
      ZMessaging.currentGlobal.tokenService.resetGlobalToken()
      Toast.makeText(getContext, getString(R.string.pref_advanced_reset_push_completed)(getContext), Toast.LENGTH_LONG).show()
      setResetEnabled(false)
      CancellableFuture.delay(5.seconds).map(_ => setResetEnabled(true))(Threading.Ui)
    }
  }

  val notificationsWebpage = returning(findById[TextButton](R.id.preferences_notifications_webpage)) { button =>
    button.onClickEvent { _ =>
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.pref_advanced_notifications_webpage_url))))
    }
  }

}

case class AdvancedBackStackKey(args: Bundle = new Bundle()) extends BackStackKey(args) {
  override def nameId: Int = R.string.pref_advanced_screen_title

  override def layoutId = R.layout.preferences_advanced

  override def onViewAttached(v: View) = {}

  override def onViewDetached() = {}
}
