package it4341.HeTroGiup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.HashMap;
@Service
@RequiredArgsConstructor
public class DSSService {
    public List<List<Double>> createDecisionTable(Map<String, Object> req) {

        List<List<?>> rawData = (List<List<?>>) req.get("initMatrix");


        List<List<Double>> data = rawData.stream()
                .map(row -> row.stream()
                        .map(item -> ((Number) item).doubleValue()) // Chuyển int, float, long... thành double hết
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        Map<String, Object> filter = (Map<String, Object>) req.get("filter");

        double fromPrice = getDouble(filter, "fromPrice");
        double toPrice = getDouble(filter, "toPrice");
        double fromDistance = getDouble(filter, "fromDistance");
        double toDistance = getDouble(filter, "toDistance");
        double fromArea = getDouble(filter, "fromArea");
        double toArea = getDouble(filter, "toArea");
        double fromSecurityPoints = getDouble(filter, "fromSecurityPoints");
        double toSecurityPoints = getDouble(filter, "toSecurityPoints");
        double fromAmenityPoints = getDouble(filter, "fromAmenityPoints");
        double toAmenityPoints = getDouble(filter, "toAmenityPoints");

        return data.stream()
                .filter(row -> {
                    double price = row.get(0);
                    double distance = row.get(1);
                    double area = row.get(2);
                    double securityPoints = row.get(3);
                    double amenityPoints = row.get(4);

                    double fitPrice = toPrice - price;
                    double fitDistance = toDistance - distance;
                    double fitArea = area - fromArea;

                    return fitPrice >= 0 &&
                            fitDistance >= 0 &&
                            fitArea >= 0 &&
                            securityPoints >= fromSecurityPoints && securityPoints <= toSecurityPoints &&
                            amenityPoints >= fromAmenityPoints && amenityPoints <= toAmenityPoints;
                })
                .toList();
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }

    public List<List<Double>> normalizeDecisionTable(Map<String, Object> req) {
        // 1. Parse dữ liệu an toàn
        @SuppressWarnings("unchecked")
        List<List<?>> rawData = (List<List<?>>) req.get("xMatrix");
        List<List<Double>> data = rawData.stream()
                .map(row -> row.stream()
                        .map(item -> ((Number) item).doubleValue())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        if (data.isEmpty()) return new ArrayList<>();

        int numCols = data.get(0).size();

        // 2. Tính mẫu số
        double[] denominators = new double[numCols];
        for (int j = 0; j < numCols; j++) {
            double sumSquare = 0.0;
            for (List<Double> row : data) {
                double value = row.get(j);
                sumSquare += value * value;
            }
            denominators[j] = Math.sqrt(sumSquare);
        }

        // 3. Chuẩn hóa và Làm tròn
        List<List<Double>> normalizedData = new ArrayList<>();
        for (List<Double> row : data) {
            List<Double> normalizedRow = new ArrayList<>();
            for (int j = 0; j < numCols; j++) {
                double value = row.get(j);
                double denominator = denominators[j];

                // Tính giá trị thô
                double normValue = (denominator == 0) ? 0.0 : (value / denominator);

                // GỌI HÀM ROUND TẠI ĐÂY
                normalizedRow.add(round(normValue));
            }
            normalizedData.add(normalizedRow);
        }

        return normalizedData;
    }

    public List<List<Double>> calculateWeightedMatrix(Map<String, Object> req) {
        // 1. LẤY LIST ĐIỂM ƯU TIÊN
        List<?> rawWeightsInput = (List<?>) req.get("weights");
        if (rawWeightsInput == null || rawWeightsInput.isEmpty()) {
            throw new IllegalArgumentException("Lỗi: Không tìm thấy danh sách 'weights'!");
        }

        // Chuyển đổi sang List<Double>
        List<Double> priorityScores = rawWeightsInput.stream()
                .map(w -> ((Number) w).doubleValue())
                .collect(Collectors.toList());

        // 2. TÍNH TỔNG ĐIỂM
        double totalScore = 0.0;
        for (Double score : priorityScores) {
            totalScore += score;
        }

        // 3. CHUẨN HÓA TRỌNG SỐ
        List<Double> normalizedWeights = new ArrayList<>();
        if (totalScore == 0) {
            for (int i = 0; i < priorityScores.size(); i++) normalizedWeights.add(0.0);
        } else {
            for (Double score : priorityScores) {
                normalizedWeights.add(score / totalScore);
            }
        }

        // 4. LẤY MA TRẬN RMATRIX
        List<List<?>> rawMatrix = (List<List<?>>) req.get("rMatrix");
        if (rawMatrix == null) return new ArrayList<>();

        List<List<Double>> rMatrix = rawMatrix.stream()
                .map(row -> row.stream()
                        .map(item -> ((Number) item).doubleValue())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        // Kiểm tra khớp số cột
        if (!rMatrix.isEmpty() && normalizedWeights.size() != rMatrix.get(0).size()) {
            throw new IllegalArgumentException("Lỗi: Số lượng weights (" + normalizedWeights.size() +
                    ") không khớp số cột dữ liệu (" + rMatrix.get(0).size() + ")");
        }

        // 5. NHÂN TRỌNG SỐ VÀO MA TRẬN
        List<List<Double>> weightedMatrix = new ArrayList<>();
        for (List<Double> row : rMatrix) {
            List<Double> weightedRow = new ArrayList<>();
            for (int j = 0; j < row.size(); j++) {
                double rVal = row.get(j);
                double wVal = normalizedWeights.get(j);

                // Nhân và gọi hàm round cho gọn
                double result = rVal * wVal;
                weightedRow.add(round(result));
            }
            weightedMatrix.add(weightedRow);
        }

        return weightedMatrix;
    }

    public Map<String, Object> calculateTOPSIS(Map<String, Object> req) {
        // 1. Lấy dữ liệu vMatrix và ép kiểu an toàn sang Double
        @SuppressWarnings("unchecked")
        List<List<?>> rawMatrix = (List<List<?>>) req.get("vMatrix");
        List<List<Double>> vMatrix = rawMatrix.stream()
                .map(row -> row.stream()
                        .map(item -> ((Number) item).doubleValue())
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        if (vMatrix.isEmpty()) return new HashMap<>();

        int rows = vMatrix.size();
        int cols = vMatrix.get(0).size();

        // 2. Tìm A+ (Max toàn bộ) và A- (Min toàn bộ)
        List<Double> aPositive = new ArrayList<>();
        List<Double> aNegative = new ArrayList<>();

        for (int j = 0; j < cols; j++) {
            double colMax = -Double.MAX_VALUE;
            double colMin = Double.MAX_VALUE;

            // Tìm max và min của cột j
            for (List<Double> row : vMatrix) {
                double val = row.get(j);
                if (val > colMax) colMax = val;
                if (val < colMin) colMin = val;
            }

            // VÌ TẤT CẢ LÀ BENEFIT:
            aPositive.add(colMax); // A+ là số lớn nhất
            aNegative.add(colMin); // A- là số nhỏ nhất
        }

        // 3. Tính khoảng cách S+ và S-
        List<Double> sPositiveList = new ArrayList<>();
        List<Double> sNegativeList = new ArrayList<>();
        List<Double> cStarList = new ArrayList<>();

        for (List<Double> row : vMatrix) {
            double sumSqDiffPos = 0.0;
            double sumSqDiffNeg = 0.0;
            for (int j = 0; j < cols; j++) {
                double val = row.get(j);
                sumSqDiffPos += Math.pow(val - aPositive.get(j), 2);
                sumSqDiffNeg += Math.pow(val - aNegative.get(j), 2);
            }

            // Tính toán giá trị thô
            double sPosRaw = Math.sqrt(sumSqDiffPos);
            double sNegRaw = Math.sqrt(sumSqDiffNeg);

            double cStarRaw;
            if (sPosRaw + sNegRaw == 0) {
                cStarRaw = 0.0;
            } else {
                cStarRaw = sNegRaw / (sPosRaw + sNegRaw);
            }

            // --- ÁP DỤNG LÀM TRÒN TẠI ĐÂY ---
            sPositiveList.add(round(sPosRaw));
            sNegativeList.add(round(sNegRaw));
            cStarList.add(round(cStarRaw));
        }

        // 5. Trả về kết quả
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("a_star", aPositive);  // A+
        result.put("a_sub", aNegative);   // A-
        result.put("s_star", sPositiveList); // S+
        result.put("s_sub", sNegativeList);  // S-
        result.put("c_star", cStarList);     // C*
        return result;
    }
    private double round(double value) {
        return (double) Math.round(value * 10000) / 10000;
    }
}