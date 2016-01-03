package com.ulmus.datastructures;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

//TODO make this extend a View and implement that way
public class QuickAmountSeekbar  {

    /**
     * Attaches a seekBar to a Edit Text making a sliding of the seek bar insert a dollar value into the editText
     * @param bar The seekBar to be attached
     * @param fillBox The EditText to be attached
     */
    boolean moving = false;
    SeekBar bar;
    EditText fillBox;

    public QuickAmountSeekbar(final SeekBar bar,final EditText fillBox,final Button less, final Button more){
        this.bar = bar;
        this.fillBox = fillBox;
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                setAmountOnBar(arg0.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                moving = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                moving = false;
            }
        });

        less.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
                double amount = Transaction.parseDollarAmount(fillBox.getText().toString());
                increamentAmount(amount,-1);
            }
        });

        more.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                double amount = Transaction.parseDollarAmount(fillBox.getText().toString());
                increamentAmount(amount,1);
            }
        });

        fillBox.addTextChangedListener(new Watcher());
        bar.setMax(28);
    }
    private void increamentAmount(double amount,double unit){
        if(amount + unit > 0) {
            setTextOnAmount(amount + unit);
        }
        else{
            setTextOnAmount(0);
        }
    }

    private void setTextOnAmount(double amount){
        String fillValue = "$" + amount;
        fillBox.setText(fillValue);
        fillBox.setSelection(fillValue.length());
    }

    private void setBarOnAmount(double amount){
        if (amount > 100)
            bar.setProgress(28);
        else if(amount > 10)
            bar.setProgress((int)Math.floor(amount/5.0)+8);
        else if(amount < 11)
            bar.setProgress((int)Math.round(amount));
    }
    private void setAmountOnBar(int progress){
        if(moving){
            int quick_amount_value = 0;

            if(progress > 9)
                quick_amount_value = (progress-8)*5;
            else
                quick_amount_value = progress;

            fillBox.setText("$"+quick_amount_value+".0");
            fillBox.setSelection((""+quick_amount_value).length()+3);
        }
    }

    public void setAmount(double amount){
        setTextOnAmount(amount);
        setBarOnAmount(amount);
    }

    private class Watcher implements TextWatcher{
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            double amount = Transaction.parseDollarAmount(fillBox.getText().toString());
            setBarOnAmount(amount);
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }

}
