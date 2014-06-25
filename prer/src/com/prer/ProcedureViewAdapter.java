package com.prer;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ProcedureViewAdapter extends BaseAdapter {
	
	private Context context;
	private List<Procedure> procedure_list;
	
	public ProcedureViewAdapter(Context context, List<Procedure> procedures) {
		super();
		this.context = context;
		procedure_list = procedures;
	}

	@Override
	public int getCount() {
		return procedure_list.size();
	}

	@Override
	public Object getItem(int position) {
		return procedure_list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ProcedureView view = new ProcedureView(context, procedure_list.get(position));
		return view;
	}
}
