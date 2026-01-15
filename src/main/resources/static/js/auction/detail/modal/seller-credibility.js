/**
 * 경로:
 * src/main/resources/static/js/auction/detail/modal/seller-credibility.js
 *
 * 역할:
 * - "판매자 정보" 버튼 클릭 시 모달 오픈
 * - GET /api/auctions/{auctionId}/seller-credibility 호출
 * - 응답 값(판매자 계정/닉네임/정상 거래/신고 이력/제재 이력) 5개 필드 렌더링
 * - 닫기(확인 버튼, X, 바깥 클릭, ESC) 지원
 *
 * 주의:
 * - 이 파일은 <script type="module"> 로 로드되어도 문제 없도록 IIFE 형태로 작성했습니다.
 */

(function initSellerCredibilityModal() {
    const root = document.querySelector("#auction-detail-page");
    if (!root) return;

    const auctionId = (root.dataset?.auctionId || "").trim();
    if (!auctionId) {
        console.warn("[seller-credibility] auctionId not found in #auction-detail-page dataset");
        return;
    }

    const openBtn = root.querySelector("#open-seller-credibility");
    if (!openBtn) return;

    // 중복 바인딩 방지
    if (openBtn.dataset.bound === "1") return;
    openBtn.dataset.bound = "1";

    // modal elements
    const modal = document.getElementById("seller-credibility-modal");
    if (!modal) {
        console.warn("[seller-credibility] #seller-credibility-modal not found");
        return;
    }

    // (옵션) 별도 overlay가 존재하는 구조도 호환
    const overlay = document.getElementById("seller-credibility-overlay"); // 없으면 null

    const closeBtn = document.getElementById("seller-credibility-close");
    const closeX = document.getElementById("seller-credibility-close-x");

    // value fields
    const $sellerAccount = document.getElementById("sc-seller-account");
    const $nickname = document.getElementById("sc-nickname");
    const $completedDealCount = document.getElementById("sc-completed-deal-count");
    const $reportCount = document.getElementById("sc-report-count");
    const $penaltyCount = document.getElementById("sc-penalty-count");

    // dialog(안쪽 박스) - 바깥 클릭 닫기에서 사용
    const dialog = modal.querySelector("div.w-full") || modal.firstElementChild;

    const state = {
        isOpen: false,
        isLoading: false,
    };

    function safeText(v, fallback = "-") {
        if (v === null || v === undefined) return fallback;
        const s = String(v).trim();
        return s ? s : fallback;
    }

    function safeNumber(v, fallback = 0) {
        const n = Number(v);
        return Number.isFinite(n) ? n : fallback;
    }

    function setLoadingUi(isLoading) {
        state.isLoading = isLoading;

        // 로딩 중엔 버튼 비활성화 (선택)
        if (closeBtn) closeBtn.disabled = !!isLoading;

        // 로딩 표시: 값 영역에 "불러오는 중..." 같은 텍스트를 넣고 싶다면 여기서 처리
        if (isLoading) {
            if ($sellerAccount) $sellerAccount.textContent = "불러오는 중...";
            if ($nickname) $nickname.textContent = "불러오는 중...";
            if ($completedDealCount) $completedDealCount.textContent = "-";
            if ($reportCount) $reportCount.textContent = "-";
            if ($penaltyCount) $penaltyCount.textContent = "-";
        }
    }

    function fillFields(data) {
        // data shape:
        // {
        //   sellerAccount: "yerincho94",
        //   nickname: "조옐",
        //   completedDealCount: 1,
        //   reportCount: 1,
        //   penaltyCount: 0
        // }

        if ($sellerAccount) $sellerAccount.textContent = safeText(data?.sellerAccount);
        if ($nickname) $nickname.textContent = safeText(data?.nickname);

        if ($completedDealCount) $completedDealCount.textContent = String(safeNumber(data?.completedDealCount, 0));
        if ($reportCount) $reportCount.textContent = String(safeNumber(data?.reportCount, 0));
        if ($penaltyCount) $penaltyCount.textContent = String(safeNumber(data?.penaltyCount, 0));
    }

    function showToast(title, message) {
        // 페이지에 이미 toast(#toast)가 있으니 있으면 그걸 쓰고, 없으면 alert fallback
        const toast = document.getElementById("toast");
        const toastTitle = document.getElementById("toastTitle");
        const toastMsg = document.getElementById("toastMsg");

        if (toast && toastTitle && toastMsg) {
            toastTitle.textContent = title || "알림";
            toastMsg.textContent = message || "";
            toast.classList.remove("hidden");
            setTimeout(() => toast.classList.add("hidden"), 3000);
            return;
        }

        // fallback
        window.alert(`${title ? title + "\n" : ""}${message || ""}`.trim());
    }

    function openModal() {
        if (state.isOpen) return;
        state.isOpen = true;

        // overlay 방식(별도 overlay가 있다면 같이 처리)
        if (overlay) {
            overlay.classList.remove("hidden");
        }

        modal.classList.remove("hidden");
        modal.classList.add("flex");

        window.addEventListener("keydown", onKeydown);
    }

    function closeModal() {
        if (!state.isOpen) return;
        state.isOpen = false;

        modal.classList.add("hidden");
        modal.classList.remove("flex");

        if (overlay) overlay.classList.add("hidden");

        window.removeEventListener("keydown", onKeydown);
    }

    function onKeydown(e) {
        if (e.key === "Escape") closeModal();
    }

    async function fetchSellerCredibility() {
        const url = `/api/auctions/${encodeURIComponent(auctionId)}/seller-credibility`;

        const res = await fetch(url, {
            method: "GET",
            headers: { Accept: "application/json" },
            credentials: "same-origin",
        });

        if (!res.ok) {
            throw new Error(`GET ${url} failed: ${res.status}`);
        }

        return await res.json();
    }

    async function openAndLoad() {
        openModal();
        setLoadingUi(true);

        try {
            const data = await fetchSellerCredibility();
            fillFields(data);
        } catch (e) {
            console.warn("[seller-credibility] fetch failed:", e);
            showToast("오류", "판매자 정보를 불러오지 못했습니다.");
            // 값은 기본값으로 유지
            if ($sellerAccount) $sellerAccount.textContent = "-";
            if ($nickname) $nickname.textContent = "-";
            if ($completedDealCount) $completedDealCount.textContent = "-";
            if ($reportCount) $reportCount.textContent = "-";
            if ($penaltyCount) $penaltyCount.textContent = "-";
        } finally {
            setLoadingUi(false);
        }
    }

    // ===== 이벤트 바인딩 =====

    // open
    openBtn.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();
        openAndLoad();
    });

    // close buttons
    closeBtn?.addEventListener("click", (e) => {
        e.preventDefault();
        closeModal();
    });

    closeX?.addEventListener("click", (e) => {
        e.preventDefault();
        closeModal();
    });

    // 바깥 클릭 닫기
    // 1) overlay가 별도이면 overlay 클릭으로 닫기
    overlay?.addEventListener("click", (e) => {
        e.preventDefault();
        closeModal();
    });

    // 2) overlay가 없으면 modal(백드랍) 클릭으로 닫기
    modal.addEventListener("click", (e) => {
        // dialog 밖을 클릭한 경우만 닫기
        if (!dialog) {
            closeModal();
            return;
        }
        if (e.target === modal) closeModal();
    });

    // dialog 클릭은 닫히지 않도록 (이벤트 버블 차단)
    dialog?.addEventListener("click", (e) => {
        e.stopPropagation();
    });
})();
