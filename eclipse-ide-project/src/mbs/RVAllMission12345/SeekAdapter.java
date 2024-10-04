package mbs.RVAllMission12345;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class SeekAdapter {
//	public static int val1;
//	public static int val2;
	public int firstVal;
	public int secondVal;
	public int progRange;
	public SeekBar seekVal1; 
	public SeekBar seekVal2; 


	public void setProgress(SeekBar seekVal1, SeekBar seekVal2, int firstVal, int secondVal, int progRange){
		this.seekVal1 = seekVal1;
		this.seekVal2 = seekVal2;
		this.firstVal = firstVal;
		this.secondVal = secondVal;
		this.progRange = progRange;
	}

	public int getFirstVal() {
		return firstVal;
	}

//	public void setFirstVal(int firstVal) {
//		this.firstVal = firstVal;
//	}

	public int getSecondVal() {
		return secondVal;
	}

//	public void setSecondVal(int secondVal) {
//		this.secondVal = secondVal;
//	}

	public void seekProgress(){
		seekVal1.setMax(progRange);
		seekVal1.setProgress(firstVal); 
		seekVal2.setMax(progRange);
		seekVal2.setProgress(secondVal); 
		
	}
	public void seekBarChange(){

		seekVal1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override 
			public void onStopTrackingTouch(SeekBar seekBarVal) { 
				// TODO Auto-generated method stub 					
			} 
			@Override 
			public void onStartTrackingTouch(SeekBar seekBarVal) { 
				// TODO Auto-generated method stub 
			} 
			@Override 
			public void onProgressChanged(SeekBar seekBarVal, int progress,boolean fromUser) { 
				// TODO Auto-generated method stub 
				firstVal = progress;
			} 
		});
		seekVal2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override 
			public void onStopTrackingTouch(SeekBar seekBarVal) { 
				// TODO Auto-generated method stub 					
			} 
			@Override 
			public void onStartTrackingTouch(SeekBar seekBarVal) { 
				// TODO Auto-generated method stub 
			} 
			@Override 
			public void onProgressChanged(SeekBar seekBarVal, int progress,boolean fromUser) { 
				// TODO Auto-generated method stub 
				secondVal = progress;
			} 
		});
		
		
	}

}