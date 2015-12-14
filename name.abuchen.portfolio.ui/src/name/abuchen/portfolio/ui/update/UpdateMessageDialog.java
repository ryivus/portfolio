package name.abuchen.portfolio.ui.update;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import com.ibm.icu.text.MessageFormat;

import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.PortfolioPlugin;
import name.abuchen.portfolio.ui.update.NewVersion.Release;

/* package */class UpdateMessageDialog extends MessageDialog
{
    private Button checkOnUpdate;
    private NewVersion newVersion;

    public UpdateMessageDialog(Shell parentShell, String title, String message, NewVersion newVersion)
    {
        super(parentShell, title, null, message, CONFIRM,
                        new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
        this.newVersion = newVersion;
    }

    @Override
    protected Control createCustomArea(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(container);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);

        createText(container);

        checkOnUpdate = new Button(container, SWT.CHECK);
        checkOnUpdate.setSelection(PortfolioPlugin.getDefault().getPreferenceStore()
                        .getBoolean(PortfolioPlugin.Preferences.AUTO_UPDATE));
        checkOnUpdate.setText(Messages.PrefCheckOnStartup);
        checkOnUpdate.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                PortfolioPlugin.getDefault().getPreferenceStore().setValue(PortfolioPlugin.Preferences.AUTO_UPDATE,
                                checkOnUpdate.getSelection());
            }
        });
        GridDataFactory.fillDefaults().grab(true, false);

        return container;
    }

    private void createText(Composite container)
    {
        StyledText text = new StyledText(container, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER);

        List<StyleRange> ranges = new ArrayList<StyleRange>();

        StringBuilder buffer = new StringBuilder();
        if (newVersion.requiresNewJavaVersion())
        {
            StyleRange style = new StyleRange();
            style.start = buffer.length();
            style.length = Messages.MsgUpdateRequiresLatestJavaVersion.length();
            style.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
            style.fontStyle = SWT.BOLD;
            ranges.add(style);

            buffer.append(Messages.MsgUpdateRequiresLatestJavaVersion);

        }

        appendReleases(buffer, ranges);

        text.setText(buffer.toString());
        text.setStyleRanges(ranges.toArray(new StyleRange[0]));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(text);
    }

    private void appendReleases(StringBuilder buffer, List<StyleRange> ranges)
    {
        Version currentVersion = FrameworkUtil.getBundle(this.getClass()).getVersion();

        for (Release release : newVersion.getReleases())
        {
            if (release.getVersion().compareTo(currentVersion) <= 0)
                continue;

            if (buffer.length() > 0)
                buffer.append("\n\n"); //$NON-NLS-1$
            String heading = MessageFormat.format(Messages.MsgUpdateNewInVersionX, release.getVersion().toString());

            StyleRange style = new StyleRange();
            style.start = buffer.length();
            style.length = heading.length();
            style.fontStyle = SWT.BOLD;
            ranges.add(style);
            buffer.append(heading);
            buffer.append("\n\n"); //$NON-NLS-1$

            for (String line : release.getLines())
                buffer.append(line).append("\n"); //$NON-NLS-1$
        }
    }
}
