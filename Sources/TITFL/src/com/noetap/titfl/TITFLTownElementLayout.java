package com.noetap.titfl;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TITFLTownElementLayout implements TITFLLayout
{
    protected TITFLActivity m_activity;
    protected TITFLPlayerView m_playerView;
    protected ImageView m_avatar;
    protected ImageView m_greeter;
    protected TITFLTownElement m_element;
    protected TextView m_greetingText;

    public TITFLTownElementLayout(Activity activity, TITFLTownElement element)
    {
        m_activity = (TITFLActivity)activity;
        m_element = element;
    }
    
    public TITFLActivity activity()
    {
        return m_activity;
    }

    public TITFLPlayerView playerView()
    {
        return m_playerView;
    }

    public TITFLTownElement element()
    {
        return m_element;
    }

    public TextView greetingText()
    {
        return m_greetingText;
    }

    @Override
    public void invalidate() 
    {
        m_playerView.invalidate();
    }

    @SuppressLint("NewApi")
    @Override
    public void initialize()
    {
        boolean reverse = m_activity.settings().m_reverseLayout;

        // This townView is just to make reverse layout easier.
        View townView = m_activity.findViewById(R.id.townView);
        if (townView == null)
        {
            reverse = false; // If townView is not found, do not apply reverse layout.
        }
        else
        {
            NoEtapUtility.setWidth(townView, TITFLTownView.width(m_activity));
            if (reverse)
            {
                NoEtapUtility.alignParentRight(townView);
            }
        }

        m_playerView = (TITFLPlayerView) m_activity.findViewById(R.id.playerView);
        m_playerView.setPlayer(m_element.visitor());
        if (reverse)
        {
            NoEtapUtility.alignParentLeft(m_playerView);
        }
        m_playerView.initialize();
        m_playerView.invalidate();

        m_greetingText = (TextView) m_activity.findViewById(R.id.textViewGreeting);
        m_greetingText.setText(m_element.greeting());
        
        m_avatar = (ImageView) m_activity.findViewById(R.id.imageViewAvatar);
        updateOutfit();

        m_greeter = (ImageView) m_activity.findViewById(R.id.imageViewGreeting);
        AnimationDrawable greeterTalk = new AnimationDrawable(); 
        greeterTalk.setOneShot(false);

        int sdkVer = android.os.Build.VERSION.SDK_INT;
        if (sdkVer < 16)
            m_greeter.setBackgroundDrawable(greeterTalk);
        else
            m_greeter.setBackground(greeterTalk);

        float factor = NoEtapUtility.getFactor(m_activity);
        int w = (int)(256 * factor);
        int h = (int)(256 * factor);
        int frame_time = 400; // msec
        Drawable d1 = NoEtapUtility.createDrawableFromAsset(m_activity, TITFLActivity.pathGreeter + "greeter_a_frm01.png", w, h);
        Drawable d2 = NoEtapUtility.createDrawableFromAsset(m_activity, TITFLActivity.pathGreeter + "greeter_a_frm02.png", w, h);
        Drawable d3 = NoEtapUtility.createDrawableFromAsset(m_activity, TITFLActivity.pathGreeter + "greeter_a_frm03.png", w, h);
        Drawable d4 = NoEtapUtility.createDrawableFromAsset(m_activity, TITFLActivity.pathGreeter + "greeter_a_frm04.png", w, h);
        greeterTalk.addFrame(d1, frame_time);
        greeterTalk.addFrame(d2, frame_time);
        greeterTalk.addFrame(d3, frame_time);
        greeterTalk.addFrame(d4, frame_time);
        m_greeter.setImageBitmap(null);
        greeterTalk.start();

        
        ImageView elementImage = (ImageView) m_activity.findViewById(R.id.imageViewBuilding);
        if (elementImage != null)
        {
            Bitmap bm = m_element.getBitmap();
            int elementImageW = (bm.getWidth() * h) / bm.getHeight();
            Drawable d = NoEtapUtility.createDrawableFromAsset(m_activity, m_element.getImageName(), elementImageW, h);
            d.setAlpha(128);
            elementImage.setImageDrawable(d);
        }

        Button buttonClose = (Button) m_activity.findViewById(R.id.buttonClose);
        setButtonActionClose(buttonClose);

        Button buttonWork = (Button) m_activity.findViewById(R.id.buttonWork);
        if (m_element.canWork())
            setButtonActionWork(buttonWork);
        else
            setButtonActionSocialize(buttonWork);
        
        Button buttonApply = (Button) m_activity.findViewById(R.id.buttonApply);
        if (m_element.canWork())
            setButtonActionApply(buttonApply);
        else
            setButtonActionExercise(buttonApply);
        
        TextView name = (TextView) m_activity.findViewById(R.id.textViewName);
        name.setText(m_element.name());
                
        ListView list = (ListView) m_activity.findViewById(R.id.listViewGoods);
        setListAction(list);
    }
    
    @Override
    public void onBackPressed()
    {
        m_activity.closeTownElement(m_element.visitor());
    }

    protected void setButtonActionClose(Button clicked)
    {
        clicked.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                m_activity.closeTownElement(m_element.visitor());
            }
        });
    }

    protected void setButtonActionWork(Button clicked)
    {
        clicked.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                if (m_element.visitor().isEmployer(m_element))
                {
                    m_element.visitor().work();
                }
                else
                {
                    NoEtapUtility.showAlert(m_activity, "Information", "You are not working here.");
                }
                m_playerView.invalidate();
            }
        });
    }
    
    protected void setButtonActionSocialize(Button clicked)
    {
        clicked.setText("Socialize");
        clicked.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                m_element.visitor().socialize();
                m_playerView.invalidate();
            }
        });
    }

    protected void setButtonActionApply(Button clicked)
    {
        final TITFLTownElementLayout layout = this;
        clicked.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                DialogApplyJob dialog = new DialogApplyJob(m_element, layout);
                dialog.show();
                m_playerView.invalidate();
            }
        });
    }

    protected void setButtonActionExercise(Button clicked)
    {
        clicked.setText("Exercise");
        clicked.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                m_element.visitor().exercise();
                m_playerView.invalidate();
            }
        });
    }

    protected void setListAction(ListView list)
    {
        if (list == null)
            return;
        
        final TITFLTownElementLayout layout = this;
        //ArrayAdapter<TITFLGoods> adapter = new ArrayAdapter<TITFLGoods>(m_activity, android.R.layout.simple_list_item_1, m_element.merchandise());
        ListAdapterGoods adapter = new ListAdapterGoods(m_activity, m_element.merchandise());
        list.setAdapter(adapter);        
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() 
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) 
            {
                final TITFLGoods goods = m_element.merchandise().get(position);
                DialogPurchaseGoods dialog = new DialogPurchaseGoods(goods, layout);
                dialog.show();
            }
        });
    }
    
    public void updateSellable()
    {
    }

    public void updateOutfit()
    {
        int w = m_element.visitor().getAvatarWidth(m_activity);
        int h = m_element.visitor().getAvatarHeight(m_activity);
        m_avatar.setImageDrawable(NoEtapUtility.createDrawableFromAsset(m_activity, m_element.visitor().outfitImage(0), w, h));
    }
}
