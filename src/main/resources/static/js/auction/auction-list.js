// /static/js/auction-list.js
document.addEventListener("DOMContentLoaded", () => {
    // 페이지네이션 링크 파라미터 정리 (항상 실행)
    cleanUpPaginationLinks();

    const minInput = document.getElementById("minPriceInput");
    const maxInput = document.getElementById("maxPriceInput");
    const form = document.querySelector('form[method="get"]');

    if (!minInput || !maxInput || !form) {
        console.warn('필터 폼 요소를 찾을 수 없습니다. 페이지 파라미터 정리만 적용됩니다.');
        return;
    }

    const numberFormatter = new Intl.NumberFormat("ko-KR");

    // 숫자만 남기고 콤마 붙이기
    function formatWithComma(el) {
        const raw = el.value.replace(/[^\d]/g, ""); // 숫자만
        if (raw.length === 0) {
            el.value = "";
            return;
        }
        // 너무 큰 값 방지(선택): 필요 없으면 제거
        // const safe = raw.slice(0, 12);

        el.value = numberFormatter.format(Number(raw));
    }

    // submit 전에 콤마 제거해서 서버에는 숫자만 가게
    function stripComma(el) {
        el.value = el.value.replace(/[^\d]/g, "");
    }

    // 입력할 때마다 콤마 포맷
    minInput.addEventListener("input", () => formatWithComma(minInput));
    maxInput.addEventListener("input", () => formatWithComma(maxInput));

    // 폼 전송 직전 콤마 제거 + 불필요한 파라미터 제거
    form.addEventListener("submit", (e) => {
        stripComma(minInput);
        stripComma(maxInput);

        // 불필요한 파라미터 제거 (빈 값 또는 기본값)
        cleanUpFormParameters(form);
    });

    /**
     * 빈 값이거나 기본값인 파라미터를 제거하여 URL을 깔끔하게 유지
     */
    function cleanUpFormParameters(formElement) {
        const formData = new FormData(formElement);

        // 제거할 파라미터 목록
        const toRemove = [];

        formData.forEach((value, key) => {
            // 빈 값 제거
            if (value === null || value === undefined || value.toString().trim() === '') {
                toRemove.push(key);
            }
            // 기본값 제거
            else if (key === 'page' && value === '0') {
                toRemove.push(key);
            }
            else if (key === 'sort' && value === 'latest') {
                toRemove.push(key);
            }
        });

        // input/select 요소 제거 (name 속성 제거하여 submit에서 제외)
        toRemove.forEach(name => {
            const elements = formElement.querySelectorAll(`[name="${name}"]`);
            elements.forEach(el => {
                // 실제 DOM에서 제거하지 않고 name만 임시 제거
                el.setAttribute('data-original-name', el.name);
                el.removeAttribute('name');
            });
        });

        // submit 후 복원 (뒤로가기 대비)
        setTimeout(() => {
            toRemove.forEach(name => {
                const elements = formElement.querySelectorAll(`[data-original-name="${name}"]`);
                elements.forEach(el => {
                    el.setAttribute('name', el.getAttribute('data-original-name'));
                    el.removeAttribute('data-original-name');
                });
            });
        }, 100);
    }

    /**
     * 페이지네이션 링크의 URL을 정리하여 불필요한 파라미터 제거
     */
    function cleanUpPaginationLinks() {
        const paginationLinks = document.querySelectorAll('nav[aria-label="Pagination"] a');

        paginationLinks.forEach(link => {
            const href = link.getAttribute('href');
            if (!href) return;

            try {
                const url = new URL(href, window.location.origin);
                const params = url.searchParams;

                // 빈 값 또는 기본값인 파라미터 제거
                const toDelete = [];

                params.forEach((value, key) => {
                    if (value === null || value === undefined || value === '') {
                        toDelete.push(key);
                    }
                    else if (key === 'page' && value === '0') {
                        toDelete.push(key);
                    }
                    else if (key === 'sort' && value === 'latest') {
                        toDelete.push(key);
                    }
                });

                toDelete.forEach(key => params.delete(key));

                // 정리된 URL로 업데이트
                link.setAttribute('href', url.pathname + (url.search || ''));
            } catch (e) {
                // URL 파싱 실패 시 원본 유지
                console.warn('Failed to clean up pagination link:', href, e);
            }
        });
    }
});
