// 구매자 거래 관리 페이지 스크립트

(() => {
    const API_URL = '/api/buyer/deals';

    const TABS = ['all', 'pending', 'ongoing', 'completed', 'cancelled'];

    // 거래 상태 -> 탭 매핑
    const STATUS_TO_TAB = {
        PENDING_CONFIRMATION: 'pending',
        CONFIRMED: 'ongoing',
        COMPLETED: 'completed',
        TERMINATED: 'cancelled',
        EXPIRED: 'cancelled',
    };

    // 상태별 배지 스타일
    const STATUS_CONFIG = {
        PENDING_CONFIRMATION: {
            label: '거래 대기',
            badgeClass: 'bg-yellow-100 text-yellow-800',
        },
        CONFIRMED: {
            label: '진행 중',
            badgeClass: 'bg-blue-100 text-blue-800',
        },
        COMPLETED: {
            label: '완료',
            badgeClass: 'bg-green-100 text-green-800',
        },
        TERMINATED: {
            label: '취소됨',
            badgeClass: 'bg-red-100 text-red-800',
        },
        EXPIRED: {
            label: '만료됨',
            badgeClass: 'bg-gray-100 text-gray-800',
        },
        DEFAULT: {
            label: '알 수 없음',
            badgeClass: 'bg-gray-100 text-gray-800',
        },
    };

    let allDeals = [];
    let currentTab = 'all';

    // 공통 fetch 유틸
    async function fetchJson(url, options = {}) {
        const resp = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
            },
            credentials: 'include',
            ...options,
        });

        if (!resp.ok) {
            let message = `요청에 실패했습니다. (HTTP ${resp.status})`;
            try {
                const data = await resp.json();
                if (data && data.message) {
                    message = data.message;
                }
            } catch (e) {
                // ignore
            }
            throw new Error(message);
        }

        return resp.json();
    }

    function formatDate(value) {
        if (!value) return '-';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '-';

        return new Intl.DateTimeFormat('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        }).format(date);
    }

    function formatPrice(value) {
        if (value == null) return '-';
        const num = Number(value);
        if (Number.isNaN(num)) return '-';
        return `${num.toLocaleString('ko-KR')}원`;
    }

    function getStatusMeta(status) {
        return STATUS_CONFIG[status] || STATUS_CONFIG.DEFAULT;
    }

    function filterDealsByTab(tabName) {
        if (tabName === 'all') return allDeals;
        return allDeals.filter(
            (deal) => STATUS_TO_TAB[deal.status] === tabName,
        );
    }

    // 탭별 카운트 업데이트 (buyer-* id 사용)
    function updateTabCounts() {
        const counts = {
            all: allDeals.length,
            pending: 0,
            ongoing: 0,
            completed: 0,
            cancelled: 0,
        };

        allDeals.forEach((deal) => {
            const tab = STATUS_TO_TAB[deal.status];
            if (tab && counts[tab] != null) {
                counts[tab] += 1;
            }
        });

        const setText = (id, value) => {
            const el = document.getElementById(id);
            if (el) el.textContent = value;
        };

        setText('buyer-all-count', counts.all);
        setText('buyer-pending-count', counts.pending);
        setText('buyer-ongoing-count', counts.ongoing);
        setText('buyer-completed-count', counts.completed);
        setText('buyer-cancelled-count', counts.cancelled);
    }

    // 카드 UI 생성 (경매 제목/썸네일 있으면 사용)
    function createDealCard(deal) {
        const statusMeta = getStatusMeta(deal.status);

        const title = deal.auctionTitle || `거래 #${deal.dealId}`;
        const thumbnailUrl =
            deal.thumbnailUrl || '/img/placeholder-image.png';

        const createdAtText = formatDate(deal.createdAt);
        const priceText = formatPrice(deal.finalPrice);

        return `
            <div class="relative flex gap-4 p-4 bg-white border border-gray-100 rounded-xl shadow-sm hover:shadow-md transition-shadow">
                <!-- 이미지 -->
                <div class="flex-shrink-0">
                    <div class="w-24 h-24 rounded-lg overflow-hidden bg-gray-100">
                        <img
                            src="${thumbnailUrl}"
                            alt="상품 이미지"
                            class="w-full h-full object-cover"
                            onerror="this.src='/img/placeholder-image.png';"
                        />
                    </div>
                </div>

                <!-- 본문 -->
                <div class="flex-1 flex flex-col justify-between min-w-0">
                    <div>
                        <div class="flex items-center gap-2">
                            <h2 class="text-base font-semibold text-gray-900 truncate">
                                ${title}
                            </h2>
                            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${statusMeta.badgeClass}">
                                ${statusMeta.label}
                            </span>
                        </div>
                        <p class="mt-1 text-xs text-gray-500">
                            생성일시 · ${createdAtText}
                        </p>
                    </div>

                    <div class="mt-3 flex items-center justify-between">
                        <div class="text-sm">
                            <span class="text-gray-500">거래 금액</span>
                            <span class="ml-2 font-semibold text-gray-900">${priceText}</span>
                        </div>

                        <div class="flex gap-2">
                            <button
                                type="button"
                                class="buyer-deal-detail-btn inline-flex items-center px-3 py-1.5 rounded-lg border border-gray-200 text-xs font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                                data-deal-id="${deal.dealId}">
                                상세보기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    // 탭 렌더링 (buyer-deal-list-*, buyer-deal-empty-* 사용)
    function renderTab(tabName) {
        const listEl = document.getElementById(`buyer-deal-list-${tabName}`);
        const emptyEl = document.getElementById(
            `buyer-deal-empty-${tabName}`,
        );
        if (!listEl || !emptyEl) return;

        const deals = filterDealsByTab(tabName);

        if (!deals.length) {
            listEl.innerHTML = '';
            emptyEl.classList.remove('hidden');
            return;
        }

        emptyEl.classList.add('hidden');
        listEl.innerHTML = deals.map(createDealCard).join('');
    }

    // 탭 전환 (buyer-tab-*, buyer-deal-tab-btn / buyer-deal-tab-content 사용)
    function switchTab(tabName) {
        currentTab = tabName;

        const tabButtons = document.querySelectorAll('.buyer-deal-tab-btn');
        const tabContents =
            document.querySelectorAll('.buyer-deal-tab-content');

        tabButtons.forEach((btn) => {
            const isActive = btn.dataset.tab === tabName;
            btn.classList.toggle('border-gray-900', isActive);
            btn.classList.toggle('text-gray-900', isActive);
            btn.classList.toggle('font-semibold', isActive);

            btn.classList.toggle('border-transparent', !isActive);
            btn.classList.toggle('text-gray-500', !isActive);
            btn.classList.toggle('font-medium', !isActive);
        });

        tabContents.forEach((content) => {
            content.classList.add('hidden');
        });

        const activeContent = document.getElementById(
            `buyer-tab-${tabName}`,
        );
        if (activeContent) {
            activeContent.classList.remove('hidden');
        }

        renderTab(tabName);
    }

    // 상세보기 버튼 클릭 -> 뷰 페이지 이동
    function bindDetailButtonHandler() {
        document.addEventListener('click', (event) => {
            const btn = event.target.closest('.buyer-deal-detail-btn');
            if (!btn) return;

            const dealId = btn.dataset.dealId;
            if (!dealId) return;

            window.location.href = `/buyer/deals/${dealId}`;
        });
    }

    function bindTabClickEvents() {
        const tabButtons = document.querySelectorAll('.buyer-deal-tab-btn');
        tabButtons.forEach((btn) => {
            btn.addEventListener('click', () => {
                const tab = btn.dataset.tab;
                if (!tab || tab === currentTab) return;
                switchTab(tab);
            });
        });
    }

    // 초기화
    async function init() {
        try {
            const data = await fetchJson(API_URL);
            // BuyerDealListResponse 구조: { buyerId, totalCount, deals: [...] }
            allDeals = data.deals ?? [];

            updateTabCounts();
            bindTabClickEvents();
            bindDetailButtonHandler();
            switchTab('all');
        } catch (error) {
            console.error(error);
            alert(error.message || '거래 정보를 불러오는데 실패했습니다.');
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
