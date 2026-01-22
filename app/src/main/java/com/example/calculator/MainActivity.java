package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay, tvHistory;
    private String currentExpression = "";
    private boolean isResultShown = false; // Biến cờ kiểm tra trạng thái Reset

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvHistory = findViewById(R.id.tvHistory);

        // Gán sự kiện cho các nút
        setButtonEvents();
    }

    private void setButtonEvents() {
        // Danh sách ID các nút nhập liệu
        int[] buttons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSub, R.id.btnMul,
                R.id.btnDiv, R.id.btnOpen, R.id.btnClose
        };

        for (int id : buttons) {
            findViewById(id).setOnClickListener(view -> {
                Button btn = (Button) view;
                String input = btn.getText().toString();

                // LOGIC YÊU CẦU: Nếu đã hiện kết quả, bấm nút bất kỳ -> Reset hoàn toàn
                if (isResultShown) {
                    currentExpression = "";
                    tvHistory.setText("");
                    isResultShown = false;
                }

                currentExpression += input;
                tvDisplay.setText(currentExpression);
            });
        }

        // Nút xóa (C)
        findViewById(R.id.btnC).setOnClickListener(v -> {
            currentExpression = "";
            tvHistory.setText("");
            tvDisplay.setText("");
            isResultShown = false;
        });

        // Nút xóa lùi (Backspace)
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isResultShown) { // Nếu đang hiện kết quả mà bấm xóa -> Reset luôn
                currentExpression = "";
                tvHistory.setText("");
                tvDisplay.setText("");
                isResultShown = false;
                return;
            }
            if (!currentExpression.isEmpty()) {
                currentExpression = currentExpression.substring(0, currentExpression.length() - 1);
                tvDisplay.setText(currentExpression);
            }
        });

        // Nút Bằng (=)
        findViewById(R.id.btnEqual).setOnClickListener(v -> calculateResult());
    }

    private void calculateResult() {
        if (currentExpression.isEmpty()) return;

        try {
            // 1. Chuyển chuỗi hiện tại lên History
            tvHistory.setText(currentExpression + " =");

            // 2. Tính toán
            double result = evaluate(currentExpression);

            // 3. Hiển thị kết quả (Nếu là số nguyên thì bỏ .0)
            if (result == (long) result) {
                tvDisplay.setText(String.format("%d", (long) result));
            } else {
                tvDisplay.setText(String.valueOf(result));
            }

            // 4. Bật cờ để lần bấm tiếp theo sẽ Reset
            isResultShown = true;

        } catch (Exception e) {
            tvDisplay.setText("Error");
            isResultShown = true;
        }
    }

    // --- BỘ MÁY TÍNH TOÁN (Xử lý chuỗi biểu thức) ---
    // Thuật toán này hỗ trợ +, -, *, /, (, ) và độ ưu tiên toán học
    public static double evaluate(String expression) {
        char[] tokens = expression.toCharArray();
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i] == ' ') continue;

            if ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.') {
                StringBuilder sbuf = new StringBuilder();
                while (i < tokens.length && ((tokens[i] >= '0' && tokens[i] <= '9') || tokens[i] == '.')) {
                    sbuf.append(tokens[i++]);
                }
                values.push(Double.parseDouble(sbuf.toString()));
                i--;
            } else if (tokens[i] == '(') {
                ops.push(tokens[i]);
            } else if (tokens[i] == ')') {
                while (ops.peek() != '(') values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.pop();
            } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(tokens[i]);
            }
        }
        while (!ops.empty()) values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        return values.pop();
    }

    public static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        return (op1 != '*' && op1 != '/') || (op2 != '+' && op2 != '-');
    }

    public static double applyOp(char op, double b, double a) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero"); return a / b;
        }
        return 0;
    }
}