/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/comparison/A_CmsDiffViewDialog.java,v $
 * Date   : $Date: 2005/11/16 12:12:55 $
 * Version: $Revision: 1.1.2.1 $
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 * All rights reserved.
 * 
 * This source code is the intellectual property of Alkacon Software GmbH.
 * It is PROPRIETARY and CONFIDENTIAL.
 * Use of this source code is subject to license terms.
 *
 * In order to use this source code, you need written permission from 
 * Alkacon Software GmbH. Redistribution of this source code, in modified 
 * or unmodified form, is not allowed unless written permission by 
 * Alkacon Software GmbH has been given.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOURCE CODE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOURCE CODE OR ITS DERIVATIVES.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 */

package org.opencms.workplace.comparison;

import com.alkacon.diff.Diff;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.tools.A_CmsHtmlIconButton;
import org.opencms.workplace.tools.CmsHtmlIconButtonStyleEnum;
import org.opencms.workplace.tools.Messages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

/**
 * Provides a GUI for the configuration file comparison dialog.<p> 
 *
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsDiffViewDialog extends CmsDialog {

    /** Diff mode. */
    private CmsDiffViewMode m_mode;

    /**
     * Default constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    protected A_CmsDiffViewDialog(CmsJspActionElement jsp) {

        super(jsp);
        setParamStyle(STYLE_NEW);
    }

    /**
     * Performs the dialog actions depending on the initialized action and displays the dialog form.<p>
     * 
     * @throws Exception if writing to the JSP out fails
     */
    public void displayDialog() throws Exception {

        if (getAction() == ACTION_CANCEL) {
            actionCloseDialog();
        }
        JspWriter out = getJsp().getJspContext().getOut();
        out.println(htmlStart());
        out.print("<link rel='stylesheet' type='text/css' href='");
        out.print(getStyleUri(getJsp()));
        out.println("diff.css'>");
        out.println(bodyStart(null));
        out.println(dialogStart());
        out.println(dialogContentStart(getParamTitle()));
        out.print("<form name='diff-form' method='post' action='");
        out.print(getDialogUri());
        out.println("'>");
        out.println(allParamsAsHidden());
        out.println("</form>");
        out.println("<p style='text-align: right;'>");
        String iconPath = null;
        String onClic = "javascript:document.forms['diff-form'].mode.value = '";
        if (getMode() == CmsDiffViewMode.ALL) {
            iconPath = A_CmsListDialog.ICON_DETAILS_HIDE;
            onClic += CmsDiffViewMode.DIFF_ONLY;
        } else {
            iconPath = A_CmsListDialog.ICON_DETAILS_SHOW;
            onClic += CmsDiffViewMode.ALL;
        }
        onClic += "'; document.forms['diff-form'].submit();";
        out.println(A_CmsHtmlIconButton.defaultButtonHtml(
            getJsp(),
            CmsHtmlIconButtonStyleEnum.SMALL_ICON_TEXT,
            "id",
            getMode().getName().key(getLocale()),
            null,
            true,
            iconPath,
            null,
            onClic));
        out.println("</p>");
        out.println(dialogBlockStart(null));
        out.println("<table cellspacing='0' cellpadding='0' class='xmlTable'>\n<tr><td><pre>");
        try {
            CmsHtmlDiffConfiguration conf = new CmsHtmlDiffConfiguration(getMode() == CmsDiffViewMode.ALL ? -1
            : getLinesBeforeSkip(), getLocale());
            out.println(Diff.diffAsHtml(getOriginalSource(), getCopySource(), conf));
        } catch (Exception e) {
            out.print(e);
        }
        out.println("</pre></td></tr>\n</table>");
        out.println(dialogBlockEnd());
        out.println(dialogContentEnd());
        out.println(dialogEnd());
        out.println(bodyEnd());
        out.println(htmlEnd());
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public CmsDiffViewMode getMode() {

        return m_mode;
    }

    /**
     * Returns the parameter value for the Mode.<p>
     *
     * @return the parameter value for the Mode
     */
    public String getParamMode() {

        if (m_mode == null) {
            return null;
        }
        return m_mode.getMode();
    }

    /**
     * Sets the parameter value for the Mode.<p>
     *
     * @param mode the parameter value for the Mode to set
     */
    public void setParamMode(String mode) {

        m_mode = CmsDiffViewMode.valueOf(mode);
    }

    /**
     * Returns the text to compare as copy.<p>
     * 
     * @return the text to compare as copy
     */
    protected abstract String getCopySource();

    /**
     * Returns the number of lines to show before they are skipped.<p>
     * 
     * @return the number of lines to show before they are skipped
     */
    protected abstract int getLinesBeforeSkip();

    /**
     * Returns the text to compare as original.<p>
     * 
     * @return the text to compare as original
     */
    protected abstract String getOriginalSource();

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamMode())) {
            // ensure a valid mode is set
            m_mode = CmsDiffViewMode.DIFF_ONLY;
        }
        // test the needed parameters
        try {
            validateParamaters();
        } catch (Exception e) {
            // close if parameters not available
            setAction(ACTION_CANCEL);
            try {
                actionCloseDialog();
            } catch (JspException e1) {
                // noop
            }
            return;
        }
    }

    /**
     * Validates the parameters.<p>
     * 
     * @throws Exception if something goes wrong
     */
    protected abstract void validateParamaters() throws Exception;

}