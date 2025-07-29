package com.example.datn_md02_admim.AdminFragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.TopProductAdapter;
import com.example.datn_md02_admim.Model.CartItem;
import com.example.datn_md02_admim.Model.Order;
import com.example.datn_md02_admim.Model.ProductStat;
import com.example.datn_md02_admim.R;
import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class HomeFragment extends Fragment {
    private EditText edtDateFrom, edtDateTo;
    private Button btnSearch;
    private BarChart filteredChart, weeklyChart, monthlyChart;
    private PieChart yearlyChart;
    private TextView tvTotalOrders, tvTotalRevenue;
    private RecyclerView listTopProducts;

    private final List<Order> completedOrders = new ArrayList<>();
    private final Map<String, ProductStat> productStats = new HashMap<>();
    private DatabaseReference orderRef;
    private final Calendar selectedFromDate = Calendar.getInstance();
    private final Calendar selectedToDate = Calendar.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);

        edtDateFrom = view.findViewById(R.id.edtDateFrom);
        edtDateTo = view.findViewById(R.id.edtDateTo);
        btnSearch = view.findViewById(R.id.btnSearch);

        filteredChart = view.findViewById(R.id.filteredRevenueChart);
        weeklyChart = view.findViewById(R.id.weeklyChart);
        monthlyChart = view.findViewById(R.id.monthlyChart);
        yearlyChart = view.findViewById(R.id.yearlyChart);

        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        listTopProducts = view.findViewById(R.id.listTopProducts);
        listTopProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        orderRef = FirebaseDatabase.getInstance().getReference("orders");

        edtDateFrom.setOnClickListener(v -> showDatePicker(edtDateFrom, selectedFromDate));
        edtDateTo.setOnClickListener(v -> showDatePicker(edtDateTo, selectedToDate));
        btnSearch.setOnClickListener(v -> searchByDateRange());

        loadData();
        return view;
    }

    private void showDatePicker(final EditText target, final Calendar calendar) {
        new DatePickerDialog(getContext(), (view, y, m, d) -> {
            calendar.set(y, m, d);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
            target.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void searchByDateRange() {
        Calendar from = (Calendar) selectedFromDate.clone();
        from.set(Calendar.HOUR_OF_DAY, 0);
        from.set(Calendar.MINUTE, 0);
        from.set(Calendar.SECOND, 0);

        Calendar to = (Calendar) selectedToDate.clone();
        to.set(Calendar.HOUR_OF_DAY, 23);
        to.set(Calendar.MINUTE, 59);
        to.set(Calendar.SECOND, 59);

        Map<String, Double> map = new TreeMap<>();
        for (Order o : completedOrders) {
            long ts = o.getTimestamp();
            if (ts >= from.getTimeInMillis() && ts <= to.getTimeInMillis()) {
                String dateStr = new SimpleDateFormat("dd/MM", Locale.getDefault()).format(new Date(ts));
                map.put(dateStr, map.getOrDefault(dateStr, 0.0) + o.getTotalAmount());
            }
        }
        renderBarChart(filteredChart, "Doanh thu lọc theo ngày", map);
    }

    private void loadData() {
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedOrders.clear();
                productStats.clear();
                int totalOrders = 0;
                double totalRevenue = 0;
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    for (DataSnapshot orderSnap : userSnap.getChildren()) {
                        Order order = orderSnap.getValue(Order.class);
                        if (order != null && "completed".equals(order.getStatus())) {
                            completedOrders.add(order);
                            totalOrders++;
                            totalRevenue += order.getTotalAmount();
                            for (CartItem item : order.getItems()) {
                                String key = item.getProductName() + (item.getVariant() != null ? "-" + item.getVariant() : "");
                                ProductStat stat = productStats.getOrDefault(key, new ProductStat(item.getProductName(), item.getVariant(), item.getProductImage()));
                                stat.add(item.getQuantity(), item.getPrice());
                                productStats.put(key, stat);
                            }
                        }
                    }
                }
                tvTotalOrders.setText(String.valueOf(totalOrders));
                tvTotalRevenue.setText("₫" + String.format("%,.0f", totalRevenue));
                listTopProducts.setAdapter(new TopProductAdapter(getContext(), new ArrayList<>(productStats.values())));
                renderCharts();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void renderCharts() {
        renderBarChart(filteredChart, "Doanh thu lọc theo ngày", groupByPeriod("day"));
        renderBarChart(weeklyChart, "Doanh thu theo tuần", groupByPeriod("week"));
        renderBarChart(monthlyChart, "Doanh thu theo tháng", groupByPeriod("month"));
        renderPieChart(yearlyChart, groupByPeriod("year"));
    }

    private Map<String, Double> groupByPeriod(String period) {
        Map<String, Double> map = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        if (period.equals("week")) {
            String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (String d : days) map.put(d, 0.0);
        } else if (period.equals("month")) {
            for (int i = 1; i <= 12; i++) map.put("T" + i, 0.0);
        }
        for (Order o : completedOrders) {
            cal.setTimeInMillis(o.getTimestamp());
            String key = "";
            if (period.equals("week")) {
                switch (cal.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY: key = "T2"; break;
                    case Calendar.TUESDAY: key = "T3"; break;
                    case Calendar.WEDNESDAY: key = "T4"; break;
                    case Calendar.THURSDAY: key = "T5"; break;
                    case Calendar.FRIDAY: key = "T6"; break;
                    case Calendar.SATURDAY: key = "T7"; break;
                    case Calendar.SUNDAY: key = "CN"; break;
                }
            } else if (period.equals("month")) {
                key = "T" + (cal.get(Calendar.MONTH) + 1);
            } else if (period.equals("year")) {
                key = new SimpleDateFormat("yyyy").format(cal.getTime());
            } else if (period.equals("day")) {
                key = new SimpleDateFormat("dd/MM").format(cal.getTime());
            }
            map.put(key, map.getOrDefault(key, 0.0) + o.getTotalAmount());
        }
        return map;
    }

    private void renderBarChart(BarChart chart, String label, Map<String, Double> data) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            entries.add(new BarEntry(i++, e.getValue().floatValue()));
            labels.add(e.getKey());
        }
        BarDataSet dataSet = new BarDataSet(entries, label);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                return String.format("%,.0f₫", value);
            }
        });
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        chart.setData(barData);
        chart.setFitBars(true);
        chart.setDescription(null);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setLabelRotationAngle(-30f);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < labels.size() ? labels.get(index) : "";
            }
        });
        chart.getAxisRight().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void renderPieChart(PieChart chart, Map<String, Double> data) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> e : data.entrySet()) {
            entries.add(new PieEntry(e.getValue().floatValue(), e.getKey()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "Doanh thu theo năm");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                return String.format("%,.0f₫", value);
            }
        });
        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.setDrawHoleEnabled(true);
        chart.setCenterText("Theo năm");
        chart.setCenterTextSize(16f);
        chart.getDescription().setEnabled(false);
        chart.animateY(1000);
        chart.invalidate();
    }
}
