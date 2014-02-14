/**
 * Copyright (c) 2011, 2014 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 */
package org.eclipse.tycho.targeteditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormColors;

public class HintBox {

    private String textValue = "";
    public static final String HINT_TEXT = "type text";
    private final Text text;
    private final List<IPropertyChangeListener> listeners = new ArrayList<IPropertyChangeListener>();

    public HintBox(Composite parent, int style) {
        this.text = new Text(parent, style);
        showHint();
        this.text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                String string = ((Text) e.getSource()).getText();
                if (!HINT_TEXT.equals(string)) {
                    setTextValue(string);
                }
            }
        });

        this.text.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(final FocusEvent e) {
                if (HintBox.this.text.getText().isEmpty()) {
                    showHint();
                    setTextValue("");
                }
            }

            @Override
            public void focusGained(final FocusEvent e) {
                if (getTextValue().isEmpty()) {
                    resetTextColor();
                    HintBox.this.text.setText("");
                }
            }
        });
    }

    protected void showHint() {
        HintBox.this.text.setText(HINT_TEXT);
        final RGB blend = FormColors.blend(text.getParent().getForeground().getRGB(), text.getParent().getBackground()
                .getRGB(), 33);
        this.text.setForeground(new Color(text.getDisplay(), blend));
    }

    private void resetTextColor() {
        this.text.setForeground(text.getParent().getForeground());
    }

    public Text getText() {
        return text;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        String oldValue = this.textValue;
        this.textValue = textValue;
        firePropertyChangedEvent(new PropertyChangeEvent(this, "textValue", oldValue, textValue));
    }

    private void firePropertyChangedEvent(PropertyChangeEvent event) {
        for (IPropertyChangeListener listener : listeners) {
            listener.propertyChange(event);
        }
    }

    public void addListener(IPropertyChangeListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        listeners.add(listener);
    }
}
