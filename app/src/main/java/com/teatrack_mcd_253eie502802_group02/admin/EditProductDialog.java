package com.teatrack_mcd_253eie502802_group02.admin;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.ArrayList;
import java.util.List;

public class EditProductDialog {

    private final Context context;
    private final Product product;
    private final List<String> categories;

    public EditProductDialog(Context context, Product product, List<String> categories) {
        this.context = context;
        this.product = product;
        this.categories = categories;
    }

    public void show() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_product);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setWindowAnimations(android.R.style.Animation_Dialog);
        }

        EditText etProductName = dialog.findViewById(R.id.etProductName);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        CheckBox cbVisible = dialog.findViewById(R.id.cbVisible);
        CheckBox cbSpecial = dialog.findViewById(R.id.cbSpecial);
        EditText etPriceM = dialog.findViewById(R.id.etPriceM);
        EditText etPriceL = dialog.findViewById(R.id.etPriceL);
        EditText etVipPriceM = dialog.findViewById(R.id.etVipPriceM);
        EditText etVipPriceL = dialog.findViewById(R.id.etVipPriceL);
        EditText etProductInfo = dialog.findViewById(R.id.etProductInfo);
        EditText etProductDesc = dialog.findViewById(R.id.etProductDesc);
        ImageButton btnClose = dialog.findViewById(R.id.btnClose);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnEdit = dialog.findViewById(R.id.btnEdit);

        // Fill Data
        etProductName.setText(product.getName());
        cbVisible.setChecked(product.isVisible());
        cbSpecial.setChecked(product.isSpecial());
        etPriceM.setText(String.valueOf(product.getPrice()));
        etPriceL.setText(String.valueOf(product.getPriceL()));
        etVipPriceM.setText(String.valueOf(product.getVipPriceM()));
        etVipPriceL.setText(String.valueOf(product.getVipPriceL()));
        etProductInfo.setText(product.getDescription());
        etProductDesc.setText(product.getDetail());

        // Setup Spinner
        List<String> spinnerCats = new ArrayList<>(categories);
        spinnerCats.remove(context.getString(R.string.filter_all));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, spinnerCats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        
        int categoryIndex = spinnerCats.indexOf(product.getCategory());
        if (categoryIndex >= 0) spinnerCategory.setSelection(categoryIndex);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnEdit.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(context, "Product name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            product.setName(name);
            product.setCategory(spinnerCategory.getSelectedItem().toString());
            product.setVisible(cbVisible.isChecked());
            product.setSpecial(cbSpecial.isChecked());
            product.setPrice(parsePrice(etPriceM));
            product.setPriceL(parsePrice(etPriceL));
            product.setVipPriceM(parsePrice(etVipPriceM));
            product.setVipPriceL(parsePrice(etVipPriceL));
            product.setDescription(etProductInfo.getText().toString().trim());
            product.setDetail(etProductDesc.getText().toString().trim());

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
            ref.setValue(product).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Product updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(context, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private int parsePrice(EditText et) {
        String val = et.getText().toString().trim();
        try {
            return val.isEmpty() ? 0 : Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
