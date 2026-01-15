document.addEventListener("DOMContentLoaded", () => {
    console.info("[deals] init");

    const role = document.getElementById("dealRole")?.value; // BUYER / SELLER
    const apiUrl = document.getElementById("dealListApi")?.value;
    const detailBasePath = document.getElementById("dealDetailBasePath")?.value;

    if (!role || !apiUrl || !detailBasePath) {
        console.error("[deals] missing config", { role, apiUrl, detailBasePath });
        return;
    }

    const tabButtons = document.querySelectorAll(".deal-tab-btn");
    const tabContents = {
        all: document.getElementById("tab-all"),
        pending: document.getElementById("tab-pending"),
        ongoing: document.getElementById("tab-ongoing"),
        completed: document.getElementById("tab-completed"),
        cancelled: document.getElementById("tab-cancelled"),
    };

    const countSpans = {
        all: document.getElementById("all-count"),
        pending: document.getElementById("pending-count"),
        ongoing: document.getElementById("ongoing-count"),
        completed: document.getElementById("completed-count"),
        cancelled: document.getElementById("cancelled-count"),
    };

    setupTabs(tabButtons, tabContents);

    fetchDeals(apiUrl)
        .then((deals) => renderAllTabs(deals, tabContents, countSpans, role, detailBasePath))
        .catch((err) => {
            console.error("[deals] fetch error", err);
            showErrorEmptyState(tabContents.all, "거래를 불러오는 중 오류가 발생했습니다.");
        });
});

// ===================== API =====================
async function fetchDeals(apiUrl) {
    const res = await fetch(apiUrl);
    if (!res.ok) throw new Error(`failed to fetch deals: ${res.status}`);

    const body = await res.json();
    const deals = body.deals ?? body.data?.deals ?? [];
    return deals.map(normalizeDeal);
}

function normalizeDeal(deal) {
    return {
        dealId: deal.dealId,
        auctionId: deal.auctionId ?? deal.auction_id ?? null,
        auctionTitle: deal.auctionTitle ?? deal.title ?? "제목 없음",
        thumbnailUrl: deal.thumbnailUrl ?? deal.auctionThumbnailUrl ?? deal.thumbnail ?? null,
        status: deal.status,
        finalPrice: deal.finalPrice ?? deal.price ?? null,
        createdAt: deal.createdAt ?? deal.created_at ?? null,
        hasReview: !!(deal.hasReview ?? false),
    };
}

// ===================== 탭 / 렌더 =====================
function setupTabs(tabButtons, tabContents) {
    tabButtons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const tab = btn.dataset.tab;
            if (!tab) return;

            tabButtons.forEach((b) => {
                b.classList.remove("border-gray-900", "font-semibold", "text-gray-900");
                b.classList.add("border-transparent", "text-gray-500");
            });

            btn.classList.remove("border-transparent", "text-gray-500");
            btn.classList.add("border-gray-900", "font-semibold", "text-gray-900");

            Object.entries(tabContents).forEach(([key, el]) => {
                if (!el) return;
                if (key === tab) el.classList.remove("hidden");
                else el.classList.add("hidden");
            });
        });
    });

    document.querySelector('.deal-tab-btn[data-tab="all"]')?.click();
}

function renderAllTabs(deals, tabContents, countSpans, role, detailBasePath) {
    const grouped = groupDealsByStatus(deals);

    countSpans.all.textContent = grouped.all.length;
    countSpans.pending.textContent = grouped.pending.length;
    countSpans.ongoing.textContent = grouped.ongoing.length;
    countSpans.completed.textContent = grouped.completed.length;
    countSpans.cancelled.textContent = grouped.cancelled.length;

    renderDealList(tabContents.all, grouped.all, "전체 거래가 없습니다", role, detailBasePath);
    renderDealList(tabContents.pending, grouped.pending, "거래 대기 중인 거래가 없습니다", role, detailBasePath);
    renderDealList(tabContents.ongoing, grouped.ongoing, "진행 중인 거래가 없습니다", role, detailBasePath);
    renderDealList(tabContents.completed, grouped.completed, "완료된 거래가 없습니다", role, detailBasePath);
    renderDealList(tabContents.cancelled, grouped.cancelled, "취소/만료된 거래가 없습니다", role, detailBasePath);
}

function groupDealsByStatus(deals) {
    const result = { all: [], pending: [], ongoing: [], completed: [], cancelled: [] };

    deals.forEach((deal) => {
        result.all.push(deal);

        switch (deal.status) {
            case "PENDING_CONFIRMATION":
                result.pending.push(deal);
                break;
            case "CONFIRMED":
                result.ongoing.push(deal);
                break;
            case "COMPLETED":
                result.completed.push(deal);
                break;
            case "TERMINATED":
            case "EXPIRED":
                result.cancelled.push(deal);
                break;
            default:
                break;
        }
    });

    return result;
}

function renderDealList(container, deals, emptyMessage, role, detailBasePath) {
    if (!container) return;

    container.innerHTML = "";

    if (!deals.length) {
        showEmptyState(container, emptyMessage);
        return;
    }

    const wrapper = document.createElement("div");
    wrapper.className = "space-y-4";

    deals.forEach((deal) => wrapper.appendChild(createDealCard(deal, role, detailBasePath)));

    container.appendChild(wrapper);
}

function showEmptyState(container, message) {
    container.innerHTML = `
    <div class="text-center py-12 bg-gray-50 rounded-lg">
      <h3 class="mt-4 text-sm font-semibold text-gray-900">${message}</h3>
      <p class="mt-2 text-sm text-gray-600">거래가 생성되면 이곳에 표시됩니다.</p>
    </div>
  `;
}

function showErrorEmptyState(container, message) {
    if (!container) return;
    container.innerHTML = `
    <div class="text-center py-12 bg-red-50 rounded-lg">
      <h3 class="mt-4 text-sm font-semibold text-red-900">오류</h3>
      <p class="mt-2 text-sm text-red-700">${message}</p>
    </div>
  `;
}

// ===================== 카드 UI =====================
function createDealCard(deal, role, detailBasePath) {
    const card = document.createElement("div");
    card.className =
        "flex gap-4 p-4 bg-white rounded-lg shadow-sm border border-gray-100 hover:shadow-md transition-shadow";

    const thumbnail = document.createElement("img");
    thumbnail.className = "w-24 h-24 rounded-md object-cover bg-gray-100 flex-shrink-0";
    thumbnail.src = deal.thumbnailUrl || "https://via.placeholder.com/96x96.png?text=No+Image";
    thumbnail.alt = deal.auctionTitle || "거래 썸네일";

    const body = document.createElement("div");
    body.className = "flex-1 flex flex-col justify-between";

    const titleRow = document.createElement("div");
    titleRow.className = "flex items-start justify-between gap-2";

    const title = document.createElement("h3");
    title.className = "text-sm font-semibold text-gray-900 line-clamp-2";
    title.textContent = deal.auctionTitle ?? "제목 없음";

    const badge = document.createElement("span");
    badge.className = "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium";
    const { badgeClass, badgeLabel } = getStatusBadgeInfo(deal.status);
    badge.className += " " + badgeClass;
    badge.textContent = badgeLabel;

    titleRow.appendChild(title);
    titleRow.appendChild(badge);

    const metaRow = document.createElement("div");
    metaRow.className = "mt-2 flex items-center gap-4 text-xs text-gray-500";

    if (deal.finalPrice != null) {
        const price = document.createElement("span");
        price.textContent = `거래 금액: ${Number(deal.finalPrice).toLocaleString()}원`;
        metaRow.appendChild(price);
    }

    if (deal.createdAt) {
        const created = document.createElement("span");
        created.textContent = `생성일: ${formatDateTime(deal.createdAt)}`;
        metaRow.appendChild(created);
    }

    const actionsRow = document.createElement("div");
    actionsRow.className = "mt-4 flex items-center justify-end gap-2";

    // 상세
    const detailBtn = document.createElement("button");
    detailBtn.type = "button";
    detailBtn.className =
        "inline-flex items-center px-3 py-1.5 border border-gray-300 rounded-md text-xs font-medium text-gray-700 bg-white hover:bg-gray-50";
    detailBtn.textContent = "상세 보기";
    detailBtn.addEventListener("click", () => {
        window.location.href = `${detailBasePath}/${deal.dealId}`;
    });
    actionsRow.appendChild(detailBtn);

    // role별 액션(최소)
    if (role === "SELLER") {
        // 판매자 확정 (선택적으로 추가하고 싶으면 여기서 모달/POST 연결)
        // deal.status === "PENDING_CONFIRMATION" 일 때 버튼 추가 가능
    } else if (role === "BUYER") {
        // 구매자 확정/수령확인 버튼은 원하면 여기서 추가 가능
    }

    // 완료 상태: 리뷰 버튼(최소)
    if (deal.status === "COMPLETED") {
        const reviewBtn = document.createElement("button");
        reviewBtn.type = "button";

        // SELLER: 리뷰 있으면 "리뷰 보기"
        // BUYER: 이미 리뷰 작성했으면 "리뷰 보기", 아니면 "리뷰 작성"
        if (role === "SELLER") {
            const canView = !!deal.hasReview;
            reviewBtn.className =
                "inline-flex items-center px-3 py-1.5 border rounded-md text-xs font-medium bg-white " +
                (canView
                    ? "border-indigo-600 text-indigo-600 hover:bg-indigo-50"
                    : "border-gray-300 text-gray-400 cursor-not-allowed");
            reviewBtn.textContent = canView ? "리뷰 보기" : "리뷰 없음";
            if (canView) {
                reviewBtn.addEventListener("click", () => {
                    window.location.href = `/seller/deals/${deal.dealId}/review/view`;
                });
            } else {
                reviewBtn.disabled = true;
            }
        } else {
            // ✅ BUYER: 리뷰 있으면 보기, 없으면 작성 화면으로 분기
            const hasReview = !!deal.hasReview;
            const reviewCreateUrl = `/buyer/deals/${deal.dealId}/review`;
            const reviewViewUrl = `/buyer/deals/${deal.dealId}/review/view`;

            reviewBtn.className =
                "inline-flex items-center px-3 py-1.5 border rounded-md text-xs font-medium bg-white border-indigo-600 text-indigo-600 hover:bg-indigo-50";
            reviewBtn.textContent = hasReview ? "리뷰 보기" : "리뷰 작성";

            reviewBtn.addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation();
                window.location.href = hasReview ? reviewViewUrl : reviewCreateUrl;
            });
        }

        actionsRow.appendChild(reviewBtn);
    }

    body.appendChild(titleRow);
    body.appendChild(metaRow);
    body.appendChild(actionsRow);

    card.appendChild(thumbnail);
    card.appendChild(body);

    return card;
}

function getStatusBadgeInfo(status) {
    switch (status) {
        case "PENDING_CONFIRMATION":
            return { badgeClass: "bg-yellow-50 text-yellow-800 border border-yellow-100", badgeLabel: "거래 대기" };
        case "CONFIRMED":
            return { badgeClass: "bg-blue-50 text-blue-800 border border-blue-100", badgeLabel: "진행 중" };
        case "COMPLETED":
            return { badgeClass: "bg-emerald-50 text-emerald-800 border border-emerald-100", badgeLabel: "완료" };
        case "TERMINATED":
        case "EXPIRED":
            return { badgeClass: "bg-gray-50 text-gray-500 border border-gray-100", badgeLabel: "취소/만료" };
        default:
            return { badgeClass: "bg-gray-50 text-gray-500 border border-gray-100", badgeLabel: status ?? "알 수 없음" };
    }
}

function formatDateTime(isoString) {
    if (!isoString) return "";
    const d = new Date(isoString);
    if (Number.isNaN(d.getTime())) return String(isoString);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mm = String(d.getMinutes()).padStart(2, "0");
    return `${y}-${m}-${day} ${hh}:${mm}`;
}
